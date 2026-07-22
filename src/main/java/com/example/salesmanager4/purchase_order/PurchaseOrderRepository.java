package com.example.salesmanager4.purchase_order;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import com.example.salesmanager4.purchase_order.dto.PoListRow;

import java.util.List;
import java.util.Optional;



public interface PurchaseOrderRepository
        extends CrudRepository<PurchaseOrder, Long> {

    public Iterable<PurchaseOrder> findAllByOrderByCreatedAtDesc();

    @Modifying
    @Query("UPDATE purchase_order SET status = :status WHERE id = :id")
    public void setStatusById(Long id, String status);

    @Query(value = """
        SELECT
            po.id AS id,
            s.name AS supplierName,
            po.order_date AS orderDate,
            po.status AS status,
            e.known_name AS createdByName,
            po.created_at AS createdAt,
            COALESCE((SELECT SUM(poi.quantity * poi.price)
                      FROM purchase_order_item poi
                      WHERE poi.purchase_order_id = po.id), 0) AS total
        FROM purchase_order po
        LEFT JOIN supplier s ON po.supplier_id = s.supplier_id
        LEFT JOIN employee e ON po.created_by = e.id
        ORDER BY po.created_at DESC
    """, rowMapperClass = PoListRowMapper.class)
    public List<PoListRow> findAllListRows();

    public Iterable<PurchaseOrder> findAllBySupplierIdOrderByCreatedAtDesc(Long supplierId);

    public Iterable<PurchaseOrder> findAllByCreatedByOrderByCreatedAtDesc(Long employeeId);

    public Iterable<PurchaseOrder> findAllByCreatedByAndSupplierIdOrderByCreatedAtDesc(Long employeeId, Long supplierId);

    public Optional<PurchaseOrder> findById(Long id);
}

class PoListRowMapper extends BeanPropertyRowMapper<PoListRow> {
    public PoListRowMapper() {
        super(PoListRow.class);
    }
}
