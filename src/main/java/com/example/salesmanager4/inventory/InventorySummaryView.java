package com.example.salesmanager4.inventory;

import java.math.BigDecimal;


// public interface InventorySummaryView {

//     Long getItemId();
//     String getCode();
//     String getName();
//     String getCategoryName();

//     BigDecimal getStockQty();
//     BigDecimal getAvgCost();

//     default BigDecimal getStockValue() {
//         if (getStockQty() == null || getAvgCost() == null) {
//             return BigDecimal.ZERO;
//         }
//         return getStockQty().multiply(getAvgCost());
//     }
// }

public record InventorySummaryView(
    Long itemid,
    String code,
    String name,
    String categoryname,
    BigDecimal stockqty,
    BigDecimal avgcost
) {

    public BigDecimal getStockValue() {
        if (stockqty() == null || avgcost() == null) {
            return BigDecimal.ZERO;
        }
        return stockqty().multiply(avgcost());
    }
}

