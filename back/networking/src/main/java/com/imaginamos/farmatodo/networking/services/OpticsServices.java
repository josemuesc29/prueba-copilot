package com.imaginamos.farmatodo.networking.services;

import com.google.api.server.spi.response.ConflictException;
import com.imaginamos.farmatodo.model.algolia.ItemAlgolia;
import com.imaginamos.farmatodo.model.item.EyeDirectionEnum;
import com.imaginamos.farmatodo.model.item.AddDeliveryOrderItemRequest;
import com.imaginamos.farmatodo.model.item.OpticalItemFilter;
import com.imaginamos.farmatodo.model.optics.*;
import com.imaginamos.farmatodo.model.order.DeliveryOrder;
import com.imaginamos.farmatodo.model.order.DeliveryOrderProvider;
import com.imaginamos.farmatodo.model.order.RequestSourceEnum;
import com.imaginamos.farmatodo.model.product.Item;
import com.imaginamos.farmatodo.networking.algolia.APIAlgolia;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

import static com.imaginamos.farmatodo.model.util.Constants.PRODUCT_OPTICS_NOT_EXISTS;


public class OpticsServices{

    private static final Logger LOG = Logger.getLogger(OpticsServices.class.getName());

    public ItemOpticsComplete generateItemOpticsComplete(ItemAlgolia itemAlgolia) {

        List<VisibleParameter> visibleParameters;

        ItemOpticsComplete itemOpticsComplete = new ItemOpticsComplete();

        if(Objects.nonNull(itemAlgolia.getItemOpticsComplete().getStepParameters())){
            visibleParameters = getVisibleParameters(itemAlgolia);
            itemOpticsComplete.setVisibleParameters(visibleParameters);
        }

        AdditionalInformationOptics additionalInformationOptics = getAdditionalInformationOptics();
        itemOpticsComplete.setItemOptics(ItemOptics.of(itemAlgolia));
        itemOpticsComplete.setAdditionalInformation(additionalInformationOptics);
        return itemOpticsComplete;
    }

    public AdditionalInformation getDeliveryTimeOptics(ItemAlgolia itemAlgolia, long idStoreGroup) {

        Optional<ConfigDeliveryTimeOptics> optionalConfigDeliveryTimeOptics = APIAlgolia.getConfigDeliveryTime();
        AdditionalInformation additionalInformation = new AdditionalInformation();
        if(Objects.nonNull(itemAlgolia.getItemOpticsComplete().getItemOptics().getShipment()) && optionalConfigDeliveryTimeOptics.isPresent()){
            ShipmentType shipmentType = itemAlgolia.getItemOpticsComplete().getItemOptics().getShipment();
            if(Objects.nonNull(shipmentType)){
                switch (shipmentType){
                    case EXPRESS:
                        additionalInformation.setType(AdditionalInformationType.DELIVERY_TIME);
                        additionalInformation.setValue(optionalConfigDeliveryTimeOptics.get().getDeliveryTimeExpress());
                        if(idStoreGroup != 26){
                            additionalInformation.setType(AdditionalInformationType.DELIVERY_TIME);
                            additionalInformation.setValue(optionalConfigDeliveryTimeOptics.get().getDeliveryTimeExtendido());
                        }
                        break;
                    case EXTENDIDO:
                        additionalInformation.setType(AdditionalInformationType.DELIVERY_TIME);
                        additionalInformation.setValue(optionalConfigDeliveryTimeOptics.get().getDeliveryTimeExtendido());
                        break;
                    default:
                        additionalInformation.setType(AdditionalInformationType.DELIVERY_TIME);
                        additionalInformation.setValue(optionalConfigDeliveryTimeOptics.get().getDeliveryTimeDefault());
                        if(idStoreGroup != 26){
                            additionalInformation.setType(AdditionalInformationType.DELIVERY_TIME);
                            additionalInformation.setValue(optionalConfigDeliveryTimeOptics.get().getDeliveryTimeExtendido());
                        }
                        break;
                }
            }
        }
        return additionalInformation;
    }

    public void addFiltersOptical(ItemAlgolia itemAlgolia, Item item, Integer quantity) {

        LOG.info("itemAlgolia.getItemOptics -> " + itemAlgolia.getItemOpticsComplete().getItemOptics().toString());

        if(Objects.nonNull(itemAlgolia.getItemOpticsComplete().getItemOptics().getParameters())){
            ParametersOptics parametersOptics = itemAlgolia.getItemOpticsComplete().getItemOptics().getParameters();
            OpticalItemFilter opticalItemFilter = new OpticalItemFilter();
            opticalItemFilter.setPower(Objects.nonNull(parametersOptics.getPower()) ? parametersOptics.getPower() : null);
            opticalItemFilter.setAxle(Objects.nonNull(parametersOptics.getAxle()) ? parametersOptics.getAxle() : null);
            opticalItemFilter.setCylinder(Objects.nonNull(parametersOptics.getCylinder()) ? parametersOptics.getCylinder() : null) ;
            opticalItemFilter.setAddition(Objects.nonNull(parametersOptics.getAddiction()) ? parametersOptics.getAddiction() : null);
            opticalItemFilter.setLensColor(Objects.nonNull(parametersOptics.getLensColor()) ? parametersOptics.getLensColor() : null);
            opticalItemFilter.setQuantity(quantity);
            opticalItemFilter.setEyeDirection(EyeDirectionEnum.SAME);
            LOG.info("itemAlgolia.getmainlensid -> " + itemAlgolia.getItemOpticsComplete().getItemOptics().getMainLensId());
            opticalItemFilter.setMainItem(itemAlgolia.getItemOpticsComplete().getItemOptics().getMainLensId());

            final String filters = opticalItemFilter.toString();
            item.setFiltersOptical(filters);
        }
    }

    @NotNull
    private AdditionalInformationOptics getAdditionalInformationOptics() {
        AdditionalInformationOptics additionalInformationOptics =new AdditionalInformationOptics();
        Optional<AdditionalInformationOptics> additionalInformationOpticsAlgolia = APIAlgolia.getAdditionalInformationOptics();
        additionalInformationOpticsAlgolia.ifPresent(informationOptics ->
                additionalInformationOptics.setAdditionalInformationList(informationOptics.getAdditionalInformationList()));
        return additionalInformationOptics;
    }


    public String getMessageConfigOpticsSame(List<OpticalItemFilter> opticalItemFilterList, boolean itemWithoutStock, RequestSourceEnum sourceEnum) {

        StringBuilder messageItemOpticsNotExits = new StringBuilder();

        Optional<MessageConfigOptics> messageConfigOpticsOptional;

        if(RequestSourceEnum.WEB.equals(sourceEnum)){
            messageConfigOpticsOptional = APIAlgolia.getMessageConfigOptics();
        }else{
            messageConfigOpticsOptional = APIAlgolia.getMessageConfigOpticsApps();
        }

        if (messageConfigOpticsOptional.isPresent()) {
            MessageConfigOptics messageConfigOptics = messageConfigOpticsOptional.get();

            if(itemWithoutStock){
                messageItemOpticsNotExits
                        .append(messageConfigOptics.getFirstMessageWithoutStock())
                        .append(" ");
            }else{
                messageItemOpticsNotExits
                        .append(messageConfigOptics.getFirstMessage())
                        .append(" ");
            }

            messageItemOpticsNotExits.append(messageConfigOptics.getNotExistsItemSameEyes());
            messageItemOpticsNotExits.append(messageConfigOptics.getSecondMessage());
            messageItemOpticsNotExits.append("<div>");
            if (RequestSourceEnum.WEB.equals(sourceEnum)) {
                messageWithParameters(opticalItemFilterList.get(0), messageItemOpticsNotExits, messageConfigOptics);
            } else {
                messageWithParametersApps(opticalItemFilterList.get(0), messageItemOpticsNotExits, messageConfigOptics);
            }
            messageItemOpticsNotExits.append("</div>");
            messageItemOpticsNotExits.append("<div>");
            if (RequestSourceEnum.WEB.equals(sourceEnum)) {
                messageWithParameters(opticalItemFilterList.get(1), messageItemOpticsNotExits, messageConfigOptics);
            } else {
                messageWithParametersApps(opticalItemFilterList.get(1), messageItemOpticsNotExits, messageConfigOptics);
            }
            messageItemOpticsNotExits.append("</div>");
        } else {
            messageItemOpticsNotExits = new StringBuilder(PRODUCT_OPTICS_NOT_EXISTS);
        }
        return String.valueOf(messageItemOpticsNotExits);
    }

    public String getMessageConfigOptics(OpticalItemFilter opticalItemFilter, boolean isSameDirection, boolean itemWithoutStock, RequestSourceEnum sourceEnum) {

        if(isSameDirection){
            opticalItemFilter.setEyeDirection(EyeDirectionEnum.SAME);
        }
        StringBuilder messageItemOpticsNotExits = new StringBuilder();

        Optional<MessageConfigOptics> messageConfigOpticsOptional;

        if(RequestSourceEnum.WEB.equals(sourceEnum)){
            messageConfigOpticsOptional = APIAlgolia.getMessageConfigOptics();
        }else{
            messageConfigOpticsOptional = APIAlgolia.getMessageConfigOpticsApps();
        }

        if (messageConfigOpticsOptional.isPresent()) {
            MessageConfigOptics messageConfigOptics = messageConfigOpticsOptional.get();

            if(itemWithoutStock){
                messageItemOpticsNotExits
                        .append(messageConfigOptics.getFirstMessageWithoutStock())
                        .append(" ");
            }else{
                messageItemOpticsNotExits
                        .append(messageConfigOptics.getFirstMessage())
                        .append(" ");
            }

            if (Objects.nonNull(opticalItemFilter.getEyeDirection())) {
                switch (opticalItemFilter.getEyeDirection()) {
                    case SAME:
                        messageItemOpticsNotExits.append(messageConfigOptics.getNotExistsItemSameEyes());
                        break;
                    case LEFT:
                        messageItemOpticsNotExits.append(messageConfigOptics.getNotExistsItemLeftEye());
                        break;
                    case RIGHT:
                        messageItemOpticsNotExits.append(messageConfigOptics.getNotExistsItemRightEye());
                        break;
                }
            }
            messageItemOpticsNotExits.append(messageConfigOptics.getSecondMessage());
            messageItemOpticsNotExits.append(" ");
            if (RequestSourceEnum.WEB.equals(sourceEnum)) {
                messageWithParameters(opticalItemFilter, messageItemOpticsNotExits, messageConfigOptics);
            } else {
                messageWithParametersApps(opticalItemFilter, messageItemOpticsNotExits, messageConfigOptics);
            }
        } else {
            messageItemOpticsNotExits = new StringBuilder(PRODUCT_OPTICS_NOT_EXISTS);
        }
        return String.valueOf(messageItemOpticsNotExits);
    }

    private static void messageWithParameters(OpticalItemFilter opticalItemFilter, StringBuilder messageItemOpticsNotExits, MessageConfigOptics messageConfigOptics) {

        if (Objects.nonNull(opticalItemFilter.getPower())) {
            messageItemOpticsNotExits.append(messageConfigOptics.getPowerParameter()).append(" ").append(opticalItemFilter.getPower());
            messageItemOpticsNotExits.append(" ");
        }
        if (Objects.nonNull(opticalItemFilter.getAxle())) {
            messageItemOpticsNotExits.append(messageConfigOptics.getAxleParameter()).append(" ").append(opticalItemFilter.getAxle());
            messageItemOpticsNotExits.append(" ");
        }
        if (Objects.nonNull(opticalItemFilter.getCylinder())) {
            messageItemOpticsNotExits.append(messageConfigOptics.getCylinderParameter()).append(" ").append(opticalItemFilter.getCylinder());
            messageItemOpticsNotExits.append(" ");
        }
        if (Objects.nonNull(opticalItemFilter.getAddition())) {
            messageItemOpticsNotExits.append(messageConfigOptics.getAdditionParameter()).append(" ").append(opticalItemFilter.getAddition());
            messageItemOpticsNotExits.append(" ");
        }
        if (Objects.nonNull(opticalItemFilter.getLensColor())) {
            messageItemOpticsNotExits.append(messageConfigOptics.getLensColorParameter()).append(" ").append(opticalItemFilter.getLensColor());
            messageItemOpticsNotExits.append(" ");
        }
    }

    private static void messageWithParametersApps(OpticalItemFilter opticalItemFilter, StringBuilder messageItemOpticsNotExits, MessageConfigOptics messageConfigOptics) {

        if (Objects.nonNull(opticalItemFilter.getPower())) {
            messageItemOpticsNotExits.append(messageConfigOptics.getPowerParameter()).append(" <h5 style='text-align: center;'><strong> ").append(opticalItemFilter.getPower());
            messageItemOpticsNotExits.append(" </strong></h5> ");
        }
        if (Objects.nonNull(opticalItemFilter.getAxle())) {
            messageItemOpticsNotExits.append(messageConfigOptics.getAxleParameter()).append(" <h5 style='text-align: center;'><strong> ").append(opticalItemFilter.getAxle());
            messageItemOpticsNotExits.append(" </strong></h5> ");
        }
        if (Objects.nonNull(opticalItemFilter.getCylinder())) {
            messageItemOpticsNotExits.append(messageConfigOptics.getCylinderParameter()).append(" <h5 style='text-align: center;'><strong> ").append(opticalItemFilter.getCylinder());
            messageItemOpticsNotExits.append(" </strong></h5> ");
        }
        if (Objects.nonNull(opticalItemFilter.getAddition())) {
            messageItemOpticsNotExits.append(messageConfigOptics.getAdditionParameter()).append(" <h5 style='text-align: center;'><strong> ").append(opticalItemFilter.getAddition());
            messageItemOpticsNotExits.append(" </strong></h5> ");
        }
        if (Objects.nonNull(opticalItemFilter.getLensColor())) {
            messageItemOpticsNotExits.append(messageConfigOptics.getLensColorParameter()).append(" <h5 style='text-align: center;'><strong> ").append(opticalItemFilter.getLensColor());
            messageItemOpticsNotExits.append(" </strong></h5> ");
        }
    }


    public void getDeliveryTimeOpticsShoppingCart(DeliveryOrder deliveryOrder, int mainIdStore) {

        String deliveryTimeOptics;
        Optional<ConfigDeliveryTimeOptics> optionalConfigDeliveryTimeOptics = APIAlgolia.getConfigDeliveryTime();
        if (optionalConfigDeliveryTimeOptics.isPresent()) {
            deliveryTimeOptics = optionalConfigDeliveryTimeOptics.get().getDeliveryTimeOrderDefault();
            List<DeliveryOrderProvider> providerList = deliveryOrder.getProviderList();
            for (DeliveryOrderProvider deliveryOrderProvider : providerList) {
                deliveryTimeOptics = getDeliveryTimeOptics(mainIdStore, deliveryTimeOptics, optionalConfigDeliveryTimeOptics, deliveryOrderProvider);
            }
        }
    }

    private static String getDeliveryTimeOptics(int mainIdStore, String deliveryTimeOptics, Optional<ConfigDeliveryTimeOptics> optionalConfigDeliveryTimeOptics, DeliveryOrderProvider deliveryOrderProvider) {
        if(deliveryOrderProvider.getId() == 1207){ // id del proveedor de optica
            List<ShipmentType> shipmentTypeList = new ArrayList<>();
            deliveryOrderProvider.getItemList().forEach(deliveryOrderItem -> {
                String id = String.valueOf(deliveryOrderItem.getId());
                String objectId = id + mainIdStore;
                ItemAlgolia itemAlgolia = APIAlgolia.getItemAlgolia(objectId);
                if (Objects.nonNull(itemAlgolia) && Objects.nonNull(itemAlgolia.getItemOpticsComplete()) &&
                        Objects.nonNull(itemAlgolia.getItemOpticsComplete().getItemOptics())
                        && Objects.nonNull(itemAlgolia.getItemOpticsComplete().getItemOptics().getShipment())) {
                    shipmentTypeList.add(itemAlgolia.getItemOpticsComplete().getItemOptics().getShipment());
                }
            });
            if (!shipmentTypeList.isEmpty()) {
                boolean containsExtendido = shipmentTypeList.contains(ShipmentType.EXTENDIDO);
                if (containsExtendido || mainIdStore != 26) {
                    deliveryTimeOptics = optionalConfigDeliveryTimeOptics.get().getDeliveryTimeOrderExtendido();
                } else {
                    deliveryTimeOptics = optionalConfigDeliveryTimeOptics.get().getDeliveryTimeOrderExpress();
                }
            }
            deliveryOrderProvider.setDeliveryTimeOptics(deliveryTimeOptics);
        }
        return deliveryTimeOptics;
    }

    @NotNull
    private List<VisibleParameter> getVisibleParameters(ItemAlgolia itemAlgolia) {

        List<VisibleParameter> visibleParameterList = new ArrayList<>();

        switch (itemAlgolia.getItemOpticsComplete().getItemOptics().getLensType()) {
            case ESFERICO:
                VisibleParameter visibleParameter = getVisibleParameter(itemAlgolia,VisibleParameterName.POWER);
                visibleParameterList.add(visibleParameter);
                break;
            case TORICO:
                VisibleParameter visibleParameterPower2 = getVisibleParameter(itemAlgolia, VisibleParameterName.POWER);
                VisibleParameter visibleParameterCylinder2 = getVisibleParameter(itemAlgolia, VisibleParameterName.CYLINDER);
                VisibleParameter visibleParameterAxle2 = getVisibleParameter(itemAlgolia, VisibleParameterName.AXLE);

                visibleParameterList.add(visibleParameterPower2);
                visibleParameterList.add(visibleParameterCylinder2);
                visibleParameterList.add(visibleParameterAxle2);
                break;
            case MULTIFOCAL:
                VisibleParameter visibleParameterPower3 = getVisibleParameter(itemAlgolia, VisibleParameterName.POWER);
                VisibleParameter visibleParameterAddition3 = getVisibleParameter(itemAlgolia, VisibleParameterName.ADDITION);

                visibleParameterList.add(visibleParameterPower3);
                visibleParameterList.add(visibleParameterAddition3);
                break;
            case COSMETICO:
                VisibleParameter visibleParameterColor = getVisibleParameter(itemAlgolia, VisibleParameterName.LENSCOLOR);

                visibleParameterList.add(visibleParameterColor);

        }
        return visibleParameterList;
    }

    @NotNull
    private VisibleParameter getVisibleParameter(ItemAlgolia itemAlgolia, VisibleParameterName visibleParameterName) {

        VisibleParameter visibleParameter = new VisibleParameter();

        Optional<ParametersLabelsOptics> parametersLabelsOpticsOptional = APIAlgolia.getLabelsParametersOptics();
        if(parametersLabelsOpticsOptional.isPresent()){
            ParametersLabelsOptics parametersLabelsOptics = parametersLabelsOpticsOptional.get();

            switch (visibleParameterName){
                case POWER:
                    visibleParameter.setName(visibleParameterName.toString().toLowerCase());
                    visibleParameter.setLabelWeb(parametersLabelsOptics.getPowerLabelWeb());
                    visibleParameter.setLabelApp(parametersLabelsOptics.getPowerLabelApp());
                    visibleParameter.setList(itemAlgolia.getItemOpticsComplete().getStepParameters().getPowerList());
                    break;
                case CYLINDER:
                    visibleParameter.setName(visibleParameterName.toString().toLowerCase());
                    visibleParameter.setLabelWeb(parametersLabelsOptics.getCylinderLabelWeb());
                    visibleParameter.setLabelApp(parametersLabelsOptics.getCylinderLabelApp());
                    visibleParameter.setList(itemAlgolia.getItemOpticsComplete().getStepParameters().getCylinderList());
                    break;
                case AXLE:
                    visibleParameter.setName(visibleParameterName.toString().toLowerCase());
                    visibleParameter.setLabelWeb(parametersLabelsOptics.getAxleLabelWeb());
                    visibleParameter.setLabelApp(parametersLabelsOptics.getAxleLabelApp());
                    visibleParameter.setList(itemAlgolia.getItemOpticsComplete().getStepParameters().getAxleList());
                    break;
                case ADDITION:
                    visibleParameter.setName(visibleParameterName.toString().toLowerCase());
                    visibleParameter.setLabelWeb(parametersLabelsOptics.getAdditionLabelWeb());
                    visibleParameter.setLabelApp(parametersLabelsOptics.getAdditionLabelApp());
                    visibleParameter.setList(itemAlgolia.getItemOpticsComplete().getStepParameters().getAdditionList());
                    break;
                case LENSCOLOR:
                    visibleParameter.setName("lensColor");
                    visibleParameter.setLabelWeb(parametersLabelsOptics.getLensColorLabelWeb());
                    visibleParameter.setLabelApp(parametersLabelsOptics.getLensColorLabelApp());
                    visibleParameter.setList(itemAlgolia.getItemOpticsComplete().getStepParameters().getColorList());
                    break;
            }
        }
        return visibleParameter;
    }

    public OpticalItemFilter getFiltersOptical(OpticalItemFilter opticalItemFilterFirstPosition, OpticalItemFilter opticalItemFilterSecondPosition) {

        OpticalItemFilter filtersOptical = new OpticalItemFilter();

        filtersOptical.setQuantity(opticalItemFilterFirstPosition.getQuantity());
        filtersOptical.setQuantitySecondPosition(opticalItemFilterSecondPosition.getQuantity());
        filtersOptical.setEyeDirection(opticalItemFilterFirstPosition.getEyeDirection());
        filtersOptical.setEyeDirectionSecondPosition(opticalItemFilterSecondPosition.getEyeDirection());

        if (Objects.nonNull(opticalItemFilterFirstPosition.getPower()) && Objects.nonNull(opticalItemFilterSecondPosition.getPower())
                && Objects.equals(opticalItemFilterFirstPosition.getPower(), opticalItemFilterSecondPosition.getPower())) {
            filtersOptical.setPower(opticalItemFilterFirstPosition.getPower());
            filtersOptical.setPowerSecondPosition(opticalItemFilterSecondPosition.getPower());
        }

        if (Objects.nonNull(opticalItemFilterFirstPosition.getCylinder()) && Objects.nonNull(opticalItemFilterSecondPosition.getCylinder())
                && Objects.equals(opticalItemFilterFirstPosition.getCylinder(), opticalItemFilterSecondPosition.getCylinder())) {
            filtersOptical.setCylinder(opticalItemFilterFirstPosition.getCylinder());
            filtersOptical.setCylinderSecondPosition(opticalItemFilterSecondPosition.getCylinder());
        }

        if (Objects.nonNull(opticalItemFilterFirstPosition.getAxle()) && Objects.nonNull(opticalItemFilterSecondPosition.getAxle())
                && Objects.equals(opticalItemFilterFirstPosition.getAxle(), opticalItemFilterSecondPosition.getAxle())) {
            filtersOptical.setAxle(opticalItemFilterFirstPosition.getAxle());
            filtersOptical.setAxleSecondPosition(opticalItemFilterSecondPosition.getAxle());
        }

        if (Objects.nonNull(opticalItemFilterFirstPosition.getAddition()) && Objects.nonNull(opticalItemFilterSecondPosition.getAddition())
                && Objects.equals(opticalItemFilterFirstPosition.getAddition(), opticalItemFilterSecondPosition.getAddition())) {
            filtersOptical.setAddition(opticalItemFilterFirstPosition.getAddition());
            filtersOptical.setAdditionSecondPosition(opticalItemFilterSecondPosition.getAddition());
        }

        if (Objects.nonNull(opticalItemFilterFirstPosition.getLensColor()) && Objects.nonNull(opticalItemFilterSecondPosition.getLensColor())
                && Objects.equals(opticalItemFilterFirstPosition.getLensColor(), opticalItemFilterSecondPosition.getLensColor())) {
            filtersOptical.setLensColor(opticalItemFilterFirstPosition.getLensColor());
            filtersOptical.setLensColorSecondPosition(opticalItemFilterSecondPosition.getLensColor());
        }
        return filtersOptical;
    }

    public boolean isEqualsFilters(OpticalItemFilter opticalItemFilterFirstPosition, OpticalItemFilter opticalItemFilterSecondPosition) {
        boolean sameFilters = true;
        List<Boolean> listBoolean = new ArrayList<>();

        listBoolean.add(isSamePower(opticalItemFilterFirstPosition, opticalItemFilterSecondPosition));
        listBoolean.add(isSameCylinder(opticalItemFilterFirstPosition, opticalItemFilterSecondPosition));
        listBoolean.add(isSameAxle(opticalItemFilterFirstPosition, opticalItemFilterSecondPosition));
        listBoolean.add(isSameAddition(opticalItemFilterFirstPosition, opticalItemFilterSecondPosition));
        listBoolean.add(isSameLensColor(opticalItemFilterFirstPosition, opticalItemFilterSecondPosition));

        if (listBoolean.contains(Boolean.FALSE)) {
            sameFilters = false;
        }
        return sameFilters;
    }


    private static Boolean isSamePower(OpticalItemFilter opticalItemFilterFirstPosition, OpticalItemFilter opticalItemFilterSecondPosition) {
        if (Objects.nonNull(opticalItemFilterFirstPosition.getPower()) && Objects.nonNull(opticalItemFilterSecondPosition.getPower())) {
            return Objects.equals(opticalItemFilterFirstPosition.getPower(), opticalItemFilterSecondPosition.getPower());
        }
        return true;
    }

    private static Boolean isSameCylinder(OpticalItemFilter opticalItemFilterFirstPosition, OpticalItemFilter opticalItemFilterSecondPosition) {
        if (Objects.nonNull(opticalItemFilterFirstPosition.getCylinder()) && Objects.nonNull(opticalItemFilterSecondPosition.getCylinder())) {
            return Objects.equals(opticalItemFilterFirstPosition.getCylinder(), opticalItemFilterSecondPosition.getCylinder());
        }
        return true;
    }

    private static Boolean isSameAxle(OpticalItemFilter opticalItemFilterFirstPosition, OpticalItemFilter opticalItemFilterSecondPosition) {
        if (Objects.nonNull(opticalItemFilterFirstPosition.getAxle()) && Objects.nonNull(opticalItemFilterSecondPosition.getAxle())) {
            return Objects.equals(opticalItemFilterFirstPosition.getAxle(), opticalItemFilterSecondPosition.getAxle());
        }
        return true;
    }

    private static Boolean isSameAddition(OpticalItemFilter opticalItemFilterFirstPosition, OpticalItemFilter opticalItemFilterSecondPosition) {
        if (Objects.nonNull(opticalItemFilterFirstPosition.getAddition()) && Objects.nonNull(opticalItemFilterSecondPosition.getAddition())) {
            return Objects.equals(opticalItemFilterFirstPosition.getAddition(), opticalItemFilterSecondPosition.getAddition());
        }
        return true;
    }

    private static Boolean isSameLensColor(OpticalItemFilter opticalItemFilterFirstPosition, OpticalItemFilter opticalItemFilterSecondPosition) {
        if (Objects.nonNull(opticalItemFilterFirstPosition.getLensColor()) && Objects.nonNull(opticalItemFilterSecondPosition.getLensColor())) {
            return Objects.equals(opticalItemFilterFirstPosition.getLensColor(), opticalItemFilterSecondPosition.getLensColor());
        }
        return true;
    }

    public void existAndStockItemsOptics(AddDeliveryOrderItemRequest addDeliveryOrderItemRequest, List<?> listBoolean, RequestSourceEnum sourceEnum, Boolean itemWithoutStock) throws ConflictException {

        if(listBoolean.size() == 1){
            if(listBoolean.get(0).equals(Boolean.TRUE)){
                throw new ConflictException(getMessageConfigOptics(addDeliveryOrderItemRequest.getOpticalItemFilterList().get(0), true, itemWithoutStock, sourceEnum));
            }
        }else{
            if (listBoolean.get(0).equals(Boolean.TRUE) && listBoolean.get(1).equals(Boolean.TRUE)){
                throw new ConflictException(getMessageConfigOpticsSame(addDeliveryOrderItemRequest.getOpticalItemFilterList(), false, sourceEnum));
            }else{
                if(listBoolean.get(0).equals(Boolean.TRUE) && listBoolean.get(1).equals(Boolean.FALSE)){
                    throw new ConflictException(getMessageConfigOptics(addDeliveryOrderItemRequest.getOpticalItemFilterList().get(0), false, itemWithoutStock, sourceEnum));
                }else if(listBoolean.get(1).equals(Boolean.TRUE) && listBoolean.get(0).equals(Boolean.FALSE)){
                    throw new ConflictException(getMessageConfigOptics(addDeliveryOrderItemRequest.getOpticalItemFilterList().get(1), false, itemWithoutStock, sourceEnum));
                }
            }
        }
    }

}
