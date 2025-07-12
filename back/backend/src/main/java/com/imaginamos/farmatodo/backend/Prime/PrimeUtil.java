package com.imaginamos.farmatodo.backend.Prime;

import com.imaginamos.farmatodo.model.algolia.PrimePlan;
import com.imaginamos.farmatodo.model.coupon.Coupon;
import com.imaginamos.farmatodo.model.order.DeliveryOrder;
import com.imaginamos.farmatodo.model.order.DeliveryOrderItem;
import com.imaginamos.farmatodo.networking.algolia.APIAlgolia;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

import static com.imaginamos.farmatodo.backend.OfyService.ofy;

public class PrimeUtil {
    private static final Logger LOG = Logger.getLogger(PrimeUtil.class.getName());

    private List<PrimePlan> primePlans = null;

    public PrimeUtil() {
        try {
            this.primePlans = APIAlgolia.primeConfig();
        } catch (Exception e) {
            LOG.info("No se encontraron prime plans configurados en Algolia");
        }
    }

    private List<PrimePlan> getPrimePlan() {
        try {
            primePlans = APIAlgolia.primeConfig();
        } catch (Exception e) {
            LOG.info("No se encontraron prime plans configurados en Algolia");
        }
        return primePlans;
    }

    public boolean isItemPrime(Long itemID) {
        List<PrimePlan> primePlans = getPrimePlan();
        if (Objects.nonNull(primePlans)) {
            return primePlans.stream().anyMatch(primePlan -> primePlan.getProduct_id().equals(itemID.toString()));
        }
        return false;
    }

    public Optional<PrimePlan> getPrimePlan(List<DeliveryOrderItem> items) {
        List<DeliveryOrderItem> primeItems = new ArrayList<>();
        if (!items.isEmpty()) {
            for (DeliveryOrderItem item : items) {
                if (isItemPrime(item.getId())) {
                    primeItems.add(item);
                }
            }
        }

        return getHighestPriorityPrimePlan(primeItems);
    }

    /*
    * El plan de mayor prioridad es el que tenga el número menor de prioridad.
    * por ejemplo entre 1, 2 y 3, será 1 la mayor prioridad
    */
    private Optional<PrimePlan> getHighestPriorityPrimePlan(List<DeliveryOrderItem> primeItems) {
        int priority = 10;
        String itemIDPriority = "0";
        List<PrimePlan> primePlans = getPrimePlan();
        if (!primeItems.isEmpty() && Objects.nonNull(primePlans)) {
            for (DeliveryOrderItem item : primeItems) {
                Long id = item.getId();
                String itemID = id.toString();
                Optional<PrimePlan> primePlanOptional = primePlans.stream().filter(
                            primePlan -> primePlan.getProduct_id().equals(itemID)
                    ).findFirst();

                if (primePlanOptional.isPresent()) {
                    int primePlanPriority = Integer.parseInt(primePlanOptional.get().getPriority());
                    if (primePlanPriority < priority) {
                        priority = primePlanPriority;
                        itemIDPriority = primePlanOptional.get().getProduct_id();
                    }
                }
            }
        }
        String finalItemIDPriority = itemIDPriority;
        LOG.info("ID del item prime con mayor prioridad -> " + finalItemIDPriority);
        return primePlans.stream().filter(
                primePlan -> primePlan.getProduct_id().equals(finalItemIDPriority)
        ).findFirst();
    }
}
