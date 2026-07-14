package com.example.salesmanager4.invoice;

import java.util.List;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

public interface InvoiceRepository extends CrudRepository<Invoice, Long> {

    @Query(value="""
        SELECT
            i.id AS id,
            i.status AS status,
            i.invoice_date AS invoiceDate,
            i.customer_id AS customerId,
            c.name AS customerName,
            i.employee_id AS employeeId,
            e.known_name AS employeeName,
            i.cash,
            i.credit,
            i.cheque,
            i.credit_due AS creditDue
        FROM invoice i
        LEFT JOIN customer c on i.customer_id = c.customer_id
        LEFT JOIN employee e on i.employee_id = e.id
        WHERE i.id = :id
    """,rowMapperClass = InvoiceRequestDtoRowMapper.class)
    public InvoiceRequestDto findRequestDtoById(@Param("id") Long id);

    @Query(value="""
        SELECT
            ii.item_id AS itemId,
            i.name AS itemName,
            ii.quantity AS quantity,
            ii.free_qty AS freeQty,
            ii.unit_price AS unitPrice,
            ii.discount AS discount
        FROM invoice_item ii
        LEFT JOIN item i on ii.item_id = i.item_id
        WHERE ii.invoice_id = :invoiceId
    """, rowMapperClass = InvoiceRequestLineDtoRowMapper.class)
    public List<InvoiceRequestLineDto> findInvoiceRequestLineDtoById(@Param("invoiceId") Long invoiceId);


    @Modifying
    @Query("UPDATE invoice SET status = :status WHERE id = :id")
    public void setStatusById(Long id, String status);
}

class InvoiceRequestDtoRowMapper extends BeanPropertyRowMapper<InvoiceRequestDto> {
    public InvoiceRequestDtoRowMapper() {
        super(InvoiceRequestDto.class);
    }
}

class InvoiceRequestLineDtoRowMapper extends BeanPropertyRowMapper<InvoiceRequestLineDto> {
    public InvoiceRequestLineDtoRowMapper() {
        super(InvoiceRequestLineDto.class);
    }
}
