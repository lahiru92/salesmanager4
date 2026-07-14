package com.example.salesmanager4.finance.payments.payable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.example.salesmanager4.finance.payments.AllocationLine;
import com.example.salesmanager4.finance.payments.PaymentDirection;
import com.example.salesmanager4.finance.payments.PaymentType;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SupplierPaymentRequest {

    private Long supplierId;
    private Long grnId;
    private PaymentType paymentMethod;
    private PaymentDirection direction;
    private BigDecimal totalPaymentAmount;
    private String chequeNumber;
    private String bank;
    private String bankAccount;
    private String referenceNumber;
    private LocalDate paymentDate;
    private List<AllocationLine> allocations = new ArrayList<>();

    public SupplierPaymentRequest(Long supplierId, Long grnId, PaymentType paymentMethod, PaymentDirection direction,
            BigDecimal totalPaymentAmount, String chequeNumber, String bank, String bankAccount,
            String referenceNumber, LocalDate paymentDate) {
        this.supplierId = supplierId;
        this.grnId = grnId;
        this.paymentMethod = paymentMethod;
        this.direction = direction;
        this.totalPaymentAmount = totalPaymentAmount;
        this.chequeNumber = chequeNumber;
        this.bank = bank;
        this.bankAccount = bankAccount;
        this.referenceNumber = referenceNumber;
        this.paymentDate = paymentDate;
    }

}
