package com.example.salesmanager4.invoice;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import lombok.Data;

@Table("invoice")
@Data
public class Invoice {

    @Id
    private Long id;
    private String status;
    private LocalDate invoiceDate;
    private Long customerId;
    private Long employeeId;

    private BigDecimal total;
    private BigDecimal cash;
    private BigDecimal cheque;
    private BigDecimal credit;
    private LocalDate creditDue;

    @MappedCollection(idColumn = "invoice_id")
    private List<InvoiceItem> items;
}
