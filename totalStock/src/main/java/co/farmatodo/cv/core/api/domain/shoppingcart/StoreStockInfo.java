package co.farmatodo.cv.core.api.domain.shoppingcart;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreStockInfo {
    private Long storeId;
    private Integer stock;
}
