package com.example.salesmanager4.purchase_order.dto;

import java.util.Date;

public record PurchaseOrderHeader(Long supplierId, Date orderDate, String itemsJson) {

}
