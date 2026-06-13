package com.example.salesmanager4.grn;

import java.util.List;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

public interface GrnRepository extends CrudRepository<Grn, Long> {

    @Query(value="""
        SELECT 
            g.id AS id,
            g.purchase_order_id AS purchaseOrderId,
            g.status AS status,
            g.received_date AS receivedDate,
            g.supplier_id AS supplierId,
            s.name AS supplierName,
            g.employee_id AS employeeId,
            e.known_name AS employeeName
        FROM grn g
        LEFT JOIN supplier s on g.supplier_id = s.supplier_id
        LEFT JOIN employee e on g.employee_id = e.id
        WHERE g.id = :id
    """,rowMapperClass = GrnRequestDtoRowMapper.class)
    public GrnRequestDto findRequestDtoById(@Param("id") Long id);

    @Query(value="""
        SELECT
            gi.item_id AS itemId,
            i.name AS itemName,
            gi.ordered_qty AS orderedQty,
            gi.received_qty AS receivedQty,
            gi.received_qty - gi.rejected_qty AS acceptedQty,
            gi.rejected_qty AS rejectedQty,
            gi.unit_price AS unitPrice,
            gi.ordered_price AS orderedPrice
        FROM grn_item gi
        LEFT JOIN item i on gi.item_id = i.item_id
        WHERE gi.grn_id = :grnId
    """, rowMapperClass = GrnRequestLineDtoRowMapper.class)
    public List<GrnRequestLineDto> findGrnRequestLineDtoById(@Param("grnId") Long grnId);


    @Modifying
    @Query("UPDATE grn SET status = :status WHERE id = :id")
    public void setStatusById(Long id, String status);
}

class GrnRequestDtoRowMapper extends BeanPropertyRowMapper<GrnRequestDto> {
    public GrnRequestDtoRowMapper() {
        super(GrnRequestDto.class);
    }
}

class GrnRequestLineDtoRowMapper extends BeanPropertyRowMapper<GrnRequestLineDto> {
    public GrnRequestLineDtoRowMapper() {
        super(GrnRequestLineDto.class);
    }
}
