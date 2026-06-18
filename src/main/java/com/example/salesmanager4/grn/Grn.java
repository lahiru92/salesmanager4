package com.example.salesmanager4.grn;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import lombok.Data;

@Table("grn")
@Data
public class Grn {

    @Id
    private Long id;
    private Long purchaseOrderId;
    private String status;
    private LocalDate receivedDate;
    private Long supplierId;
    private Long employeeId;

    private BigDecimal cash;
    private BigDecimal cheque;
    private BigDecimal credit;

    @MappedCollection(idColumn = "grn_id")
    private List<GrnItem> items;
}
