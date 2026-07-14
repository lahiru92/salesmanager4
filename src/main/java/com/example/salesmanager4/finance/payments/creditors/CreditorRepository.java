package com.example.salesmanager4.finance.payments.creditors;

import java.util.List;

import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.salesmanager4.finance.payments.creditors.dto.SupplierAging;
import com.example.salesmanager4.finance.payments.creditors.dto.SupplierOutstandingBalance;
import com.example.salesmanager4.finance.payments.creditors.dto.SupplierOutstandingGrn;
import com.example.salesmanager4.finance.payments.creditors.dto.SupplierUnallocatedCredits;
import com.example.salesmanager4.finance.payments.creditors.dto.SupplierUnallocatedRefunds;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CreditorRepository {

    private final JdbcTemplate jdbcTemplate;

    public List<SupplierOutstandingBalance> getSupplierOutstandingBalances() {
        String sql = "select * from outstanding_balance_per_supplier";
        return jdbcTemplate.query(sql, new DataClassRowMapper<>(SupplierOutstandingBalance.class));
    }

    public List<SupplierOutstandingGrn> getSupplierOutstandingGrns() {
        String sql = "select * from outstanding_grns_per_supplier";
        return jdbcTemplate.query(sql, new DataClassRowMapper<>(SupplierOutstandingGrn.class));

    }

    public List<SupplierOutstandingGrn> getSupplierOutstandingGrns(Long supplierId) {
        String sql = "select * from outstanding_grns_per_supplier where supplier_id = ?";
        return jdbcTemplate.query(sql, new DataClassRowMapper<>(SupplierOutstandingGrn.class), supplierId);
    }

    public List<SupplierAging> getSupplierAging() {
        String sql = "select * from supplier_aging";

        return jdbcTemplate.query(sql, (rs, rowNum) -> new SupplierAging(
                rs.getLong("supplier_id"),
                rs.getString("supplier_name"),
                rs.getBigDecimal("total_outstanding"),
                rs.getBigDecimal("current_amount"),
                rs.getBigDecimal("overdue_1_30"),
                rs.getBigDecimal("overdue_31_60"),
                rs.getBigDecimal("overdue_61_90"),
                rs.getBigDecimal("overdue_90_plus")));
    }

    public List<SupplierUnallocatedCredits> getUnallocatedPaymentCrPerSupplier() {
        String sql = "select * from unallocated_payment_cr_per_supplier";

        return jdbcTemplate.query(sql, new DataClassRowMapper<>(SupplierUnallocatedCredits.class));
    }

    public List<SupplierUnallocatedRefunds> getUnpostedSupplierRefunds() {
        String sql = "select * from unposted_supplier_refunds";

        return jdbcTemplate.query(sql, new DataClassRowMapper<>(SupplierUnallocatedRefunds.class));
    }

}
