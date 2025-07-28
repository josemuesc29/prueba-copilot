package co.farmatodo.cv.core.api.domain.shoppingcart;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IndividualStoreStockResponseDomain {
    private List<StoreStockInfo> stocks;
}
