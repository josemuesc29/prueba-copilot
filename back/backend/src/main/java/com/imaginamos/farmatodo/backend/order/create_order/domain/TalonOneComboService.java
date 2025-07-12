package com.imaginamos.farmatodo.backend.order.create_order.domain;

import com.algolia.search.exceptions.AlgoliaException;
import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.ConflictException;
import com.google.api.server.spi.response.NotFoundException;
import com.imaginamos.farmatodo.backend.cache.datasources.DatastoreAPI;
import com.imaginamos.farmatodo.backend.product.ProductsMethods;
import com.imaginamos.farmatodo.backend.stock.StockMethods;
import com.imaginamos.farmatodo.model.item.AddDeliveryOrderItemRequest;
import com.imaginamos.farmatodo.model.order.DeliveryOrder;
import com.imaginamos.farmatodo.model.order.DeliveryOrderItem;
import com.imaginamos.farmatodo.model.product.Item;
import com.imaginamos.farmatodo.model.stock.TotalStockRequest;
import com.imaginamos.farmatodo.model.stock.TotalStockResponse;
import com.imaginamos.farmatodo.model.util.Constants;
import com.imaginamos.farmatodo.model.util.DeliveryType;
import com.imaginamos.farmatodo.networking.api.ShoppingCartApi;
import com.imaginamos.farmatodo.networking.base.ApiBuilder;
import com.imaginamos.farmatodo.networking.growthbook.GrowthBookConfigLoader;
import com.imaginamos.farmatodo.networking.talonone.TalonOneService;
import com.imaginamos.farmatodo.networking.talonone.model.ComboDetail;
import com.imaginamos.farmatodo.networking.talonone.model.ItemCombo;
import com.imaginamos.farmatodo.backend.order.create_order.infraestructure.OrderEndpoint;
import com.imaginamos.farmatodo.networking.talonone.util.ComboCalculator;
import com.imaginamos.farmatodo.networking.algolia.APIAlgolia;
import com.imaginamos.farmatodo.model.algolia.tips.TipConfig;
import org.springframework.util.CollectionUtils;
import retrofit2.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class TalonOneComboService {
    private static final Logger LOG = Logger.getLogger(TalonOneComboService.class.getName());

    private final OrderEndpoint orderEndpoint;
    private final ProductsMethods productsMethods;
    private final StockMethods stockMethods;
    private final ShoppingCartApi shoppingCartApi;

    public TalonOneComboService(OrderEndpoint orderEndpoint) {
        this.orderEndpoint = orderEndpoint;
        productsMethods = new ProductsMethods();
        stockMethods = new StockMethods();
        shoppingCartApi = ApiBuilder.get().createShoppingCartService(ShoppingCartApi.class);
    }

    public TalonOneComboService(ProductsMethods productsMethods) {
        shoppingCartApi = ApiBuilder.get().createShoppingCartService(ShoppingCartApi.class);
        orderEndpoint = null;
        this.productsMethods = productsMethods;
        stockMethods = new StockMethods();
    }

    public TalonOneComboService() {
        shoppingCartApi = ApiBuilder.get().createShoppingCartService(ShoppingCartApi.class);
        orderEndpoint = null;
        productsMethods = new ProductsMethods();
        this.stockMethods = new StockMethods();
    }

    public void validAndSaveItemsCombo(String storeId, String idCustomerWebSafe,
                                       String token, String tokenIdWebSafe,
                                       DeliveryOrder deliveryOrderSavedShoppingCart, DeliveryType deliveryType) {
        // Cargar la lista de DeliveryOrderItems asociados
        List<DeliveryOrderItem> deliveryOrderItemList = DatastoreAPI.getDeliveryOrderItemsByDeliveryOrder(
                deliveryOrderSavedShoppingCart);

        // Si la lista está vacía, no hay nada que hacer
        if (deliveryOrderItemList.isEmpty()) {
            LOG.info("No se encontraron DeliveryOrderItems para el DeliveryOrder proporcionado.");
            return;
        }

        // Inicializar el servicio TalonOne
        TalonOneService talonOneService = new TalonOneService();
        List<ComboDetail> itemsCombo = talonOneService.sendOrderComboToTalonOne(deliveryOrderItemList);

        if (itemsCombo.isEmpty()) {
            LOG.info("No se encontraron combos en TalonOne para los DeliveryOrderItems proporcionados.");
            return;
        }

        setQuantityCombo(itemsCombo, deliveryOrderItemList);

        // Parsing storeId una sola vez
        int storeIdInt;
        try {
            storeIdInt = Integer.parseInt(storeId);
        } catch (NumberFormatException e) {
            LOG.warning("storeId no es un número válido: " + storeId + ". Operación abortada.");
            return;
        }

        Optional<TipConfig> tipConfigOptional = GrowthBookConfigLoader.getTipsConfig(Constants.CUSTOMER_ANONYMOUS);

        AtomicReference<Optional<String>> itemTipId = new AtomicReference<>(Optional.empty());

        tipConfigOptional.ifPresent(tipConfig -> extractItemTipId(tipConfig, deliveryOrderItemList, itemTipId));

        // Procesar cada ItemCombo
        addItemsByCombo(idCustomerWebSafe, token, tokenIdWebSafe, itemsCombo, storeIdInt, deliveryOrderItemList);

        addTip(idCustomerWebSafe, token, tokenIdWebSafe, deliveryType, itemTipId, storeIdInt);

        // Eliminar el DeliveryOrderItem original si idCombo no está vacío
        deleteItemCombo(idCustomerWebSafe, token, tokenIdWebSafe, itemsCombo, storeIdInt);
    }

    private void addTip(String idCustomerWebSafe, String token, String tokenIdWebSafe, DeliveryType deliveryType, AtomicReference<Optional<String>> itemTipId, int storeIdInt) {
        if (itemTipId.get().isPresent()) {
            try {
                orderEndpoint.addDeliveryOrderItem(
                        token,
                        tokenIdWebSafe,
                        idCustomerWebSafe,
                        Integer.valueOf(itemTipId.get().get()),
                        1,
                        storeIdInt,
                        false,
                        deliveryType.toString(),
                        "ADDED_FROM_SEARCH",
                        "TIP",
                        false,
                        null,
                        null,
                        null,
                        null
                );
            } catch (Exception e) {
                LOG.warning("Error al guardar el ItemTip: " + itemTipId.get().get());
            }
        }
    }

    private void extractItemTipId(TipConfig tipConfig, List<DeliveryOrderItem> deliveryOrderItemList, AtomicReference<Optional<String>> itemTipId) {
        deliveryOrderItemList.forEach(itemCart -> {
            if (tipConfig != null && tipConfig.getItemTips() != null) {
                tipConfig.getItemTips().forEach(itemTip -> {
                    if (itemTip != null && itemTip.getItemId() != null && itemCart.getId() == itemTip.getItemId()) {
                        itemTipId.set(Optional.of(String.valueOf(itemCart.getId())));
                    }
                });
            }
        });
    }

    private void deleteItemCombo(String idCustomerWebSafe, String token, String tokenIdWebSafe, List<ComboDetail> comboDetails, int storeIdInt) {
        if (!comboDetails.isEmpty()) {
            for (ComboDetail comboDetail : comboDetails) {
                try {
                    int idComboInt = Integer.parseInt(comboDetail.getComboSku());
                    orderEndpoint.deleteDeliveryOrderItem(
                            token,
                            tokenIdWebSafe,
                            idCustomerWebSafe,
                            storeIdInt,
                            idComboInt,
                            null
                    );
                } catch (NumberFormatException e) {
                    LOG.warning("idCombo no es un número válido: " + comboDetail.getComboSku() + ". No se pudo eliminar el ItemCombo original.");
                } catch (ConflictException | BadRequestException | AlgoliaException e) {
                    LOG.warning("Error al eliminar el ItemCombo original con idCombo: " + comboDetail.getComboSku() + ". Detalle: " + e.getMessage());
                }
            }
        }
    }

    private void addItemsByCombo(String idCustomerWebSafe, String token, String tokenIdWebSafe,
                                 List<ComboDetail> comboDetails, int storeIdInt,
                                 List<DeliveryOrderItem> deliveryOrderItemList) {
        for (ComboDetail comboDetail : comboDetails) {
            var quantity = comboDetail.getQuantity();
            if (quantity == 0) {
                continue;
            }
            for (ItemCombo item : comboDetail.getItemsCombo()) {
                try {
                    int itemId = Integer.parseInt(item.getId());
                    int itemQuantity = Integer.parseInt(item.getQuantity()) * quantity;

                    Optional<DeliveryOrderItem> matchingItem = deliveryOrderItemList.stream()
                            .filter(orderItem -> orderItem.getId() == Long.parseLong(item.getId()))
                            .findFirst();
                    if (matchingItem.isPresent()) {
                        itemQuantity += matchingItem.get().getQuantitySold();
                    }

                    orderEndpoint.addDeliveryOrderItem(
                            token,
                            tokenIdWebSafe,
                            idCustomerWebSafe,
                            itemId,
                            itemQuantity,
                            storeIdInt,
                            true,
                            null,
                            "undefined",
                            null,
                            null,
                            null,
                            null,
                            null,
                            null
                    );

                } catch (NumberFormatException e) {
                    LOG.warning("Error al parsear id o cantidad del ItemCombo: " + item + ". Detalle: " + e.getMessage());
                } catch (ConflictException | BadRequestException |
                         AlgoliaException | NotFoundException e) {
                    LOG.warning("Error al guardar el ItemCombo: " + item.getId() + ". Detalle: " + e.getMessage());
                }
            }
        }
    }

    private static void setQuantityCombo(List<ComboDetail> comboDetails, List<DeliveryOrderItem> deliveryOrderItemList) {

        for(ComboDetail comboDetail : comboDetails){
            if (comboDetail.getComboSku() == null || comboDetail.getComboSku().isEmpty()) {
                continue;
            }
            try {
                long idComboLong = Long.parseLong(comboDetail.getComboSku());

                // Buscar DeliveryOrderItem por idCombo
                Optional<DeliveryOrderItem> matchingItem = deliveryOrderItemList.stream()
                        .filter(item -> item.getId() == idComboLong)
                        .findFirst();

                matchingItem.ifPresent(deliveryOrderItem -> comboDetail.setQuantity(deliveryOrderItem.getQuantitySold()));
            }  catch (NumberFormatException e) {
                LOG.warning("comboSku inválido: " + comboDetail.getComboSku());
            }

        }
    }

    public Item totalStockIfIsItemComboTalon(
            int id,
            int idStoreGroup,
            Integer quantity,
            AddDeliveryOrderItemRequest request,
            boolean isScanAndGo,
            Item item,
            boolean useTotalStock) {

        DeliveryOrderItem deliveryOrderItem = createDeliveryOrderItem(item, quantity);
        List<DeliveryOrderItem> deliveryOrderItems = Collections.singletonList(deliveryOrderItem);

        List<ComboDetail> comboDetails = getItemComboFromTalon(deliveryOrderItems);

        if (CollectionUtils.isEmpty(comboDetails)) {
            return item;
        }

        if (Objects.isNull(request.getNearbyStores()) && useTotalStock) {
            List<Integer> nearbyStores = new ArrayList<>();
            request.setNearbyStores(nearbyStores);
        }

        int totalStockReal = getTotalStockReal(id, idStoreGroup, request.getNearbyStores(), isScanAndGo, quantity, useTotalStock, comboDetails);

        item.setTotalStock(totalStockReal);
        return item;
    }

    public int getTotalStockReal(int id,
                                  int idStoreGroup,List<Integer> nearbyStores,
                                  boolean isScanAndGo,Integer quantity,
                                  boolean useTotalStock, List<ComboDetail> comboDetails) {

        List<Item> itemsCombo = comboDetails.get(0).getItemsCombo().stream()
                .map(combo -> mapItemCombo(combo, idStoreGroup, quantity))
                .collect(Collectors.toList());
        LOG.info("ItemsCombo: " + itemsCombo);

        validItemsCombo(itemsCombo, nearbyStores, id, idStoreGroup, isScanAndGo, quantity, useTotalStock);

        return calculateTotalStock(itemsCombo);
    }

    private DeliveryOrderItem createDeliveryOrderItem(Item item, Integer quantity) {
        DeliveryOrderItem deliveryOrderItem = new DeliveryOrderItem();
        deliveryOrderItem.setId(item.getId());
        deliveryOrderItem.setMediaDescription(item.getMediaDescription());
        deliveryOrderItem.setQuantitySold(quantity);
        deliveryOrderItem.setFullPrice(item.getFullPrice());
        deliveryOrderItem.setOfferPrice(item.getOfferPrice());
        deliveryOrderItem.setCategorie(item.getCategorie());
        deliveryOrderItem.setMarca(item.getMarca());
        deliveryOrderItem.setBrand(item.getBrand());
        deliveryOrderItem.setSubCategory(item.getSubCategory());
        deliveryOrderItem.setRms_class(item.getRms_class());
        deliveryOrderItem.setRms_subclass(item.getRms_subclass());
        return deliveryOrderItem;
    }

    private List<ComboDetail> getItemComboFromTalon(List<DeliveryOrderItem> deliveryOrderItems) {
        TalonOneService talonOneService = new TalonOneService();
        try {
            return talonOneService.sendOrderComboToTalonOne(deliveryOrderItems);
        } catch (Exception e) {
            LOG.warning("Error al obtener los combos de TalonOne: " + e.getMessage());
        }
        return Collections.emptyList();
    }

    private Item mapItemCombo(ItemCombo itemCombo, int idStoreGroup, Integer quantity) {
        Item itemComboAlgolia = productsMethods.setFindInformationToAlgoliaByIdItem(
                itemCombo.getId(), idStoreGroup, quantity);
        itemComboAlgolia.setQuantityRequest(Integer.valueOf(itemCombo.getQuantity()));
        return itemComboAlgolia;
    }

    private void validItemsCombo(
            List<Item> itemsCombo,
            List<Integer> nearbyStores,
            int id,
            int idStoreGroup,
            boolean isScanAndGo,
            Integer quantity,
            boolean useTotalStock) {

        itemsCombo.replaceAll(itemCombo -> {
            try {
                if (useTotalStock) {
                    return stockMethods.validateStockItem(itemCombo, nearbyStores, id, isScanAndGo);
                } else {
                    return productsMethods.validateItemAlgolia(itemCombo, idStoreGroup, id, isScanAndGo, quantity);
                }
            } catch (Exception e) {
                LOG.warning("Error al validar el stock de los items de combo: " + e.getMessage());
            }
            return itemCombo;
        });
    }

    private int calculateTotalStock(List<Item> itemsCombo) {
        int[] stock = itemsCombo.stream()
                .mapToInt(Item::getTotalStock)
                .toArray();

        int[] unitsRequired = itemsCombo.stream()
                .mapToInt(Item::getQuantityRequest)
                .toArray();

        return ComboCalculator.maxCombos(stock, unitsRequired);
    }

    public void ifComboSetTotalStock(List<Long> nearbyStoresList, Item item, int storeId) {
        List<Integer> nearbyStores = nearbyStoresList.stream().map(Long::intValue).collect(Collectors.toList());
        DeliveryOrderItem deliveryOrderItem = createDeliveryOrderItem(item, 1);
        List<DeliveryOrderItem> deliveryOrderItems = Collections.singletonList(deliveryOrderItem);

        List<ComboDetail> comboDetails = getItemComboFromTalon(deliveryOrderItems);

        if (CollectionUtils.isEmpty(comboDetails)) {
            return;
        }

        List<Item> itemsCombo = comboDetails.get(0).getItemsCombo().stream()
                .map(combo -> mapItemCombo(combo, storeId, comboDetails.get(0).getQuantity()))
                .toList();

        itemsCombo.forEach(itemCombo -> {
            TotalStockRequest totalStockRequest = new TotalStockRequest(itemCombo.getId(), nearbyStores);
            try {
                Response<TotalStockResponse> response = shoppingCartApi.getTotalStock(totalStockRequest).execute();
                if (response.isSuccessful() && response.body() != null) {
                    TotalStockResponse totalStockResponse = response.body();
                    itemCombo.setTotalStock(totalStockResponse.getData().getTotalStock());
                } else {
                    LOG.warning("Failed to get total stock: " + response.errorBody());
                }
            } catch (IOException e) {
                LOG.warning("Error al validar el stock de los items de combo: " + e.getMessage());
            }
        });

        int totalStockReal = calculateTotalStock(itemsCombo);
        item.setTotalStock(totalStockReal);
    }
}
