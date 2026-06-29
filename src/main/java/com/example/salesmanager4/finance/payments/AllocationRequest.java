package com.example.salesmanager4.finance.payments;

import java.math.BigDecimal;

public record AllocationRequest(Long documentId, BigDecimal allocatedAmount) {

}
