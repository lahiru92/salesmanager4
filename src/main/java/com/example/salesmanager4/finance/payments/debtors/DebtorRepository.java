package com.example.salesmanager4.finance.payments.debtors;

import java.util.List;

import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.salesmanager4.finance.payments.debtors.dto.CustomerAging;
import com.example.salesmanager4.finance.payments.debtors.dto.CustomerOutstandingBalance;
import com.example.salesmanager4.finance.payments.debtors.dto.CustomerOutstandingInvoice;
import com.example.salesmanager4.finance.payments.debtors.dto.CustomerUnallocatedCredits;
import com.example.salesmanager4.finance.payments.debtors.dto.CustomerUnallocatedRefunds;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class DebtorRepository {

    private final JdbcTemplate jdbcTemplate;

    public List<CustomerOutstandingBalance> getCustomerOutstandingBalances() {
        String sql = "select * from outstanding_balance_per_customer";
        return jdbcTemplate.query(sql, new DataClassRowMapper<>(CustomerOutstandingBalance.class));
    }

    public List<CustomerOutstandingInvoice> getCustomerOutstandingInvoices() {
        String sql = "select * from outstanding_invoices_per_customer";
        return jdbcTemplate.query(sql, new DataClassRowMapper<>(CustomerOutstandingInvoice.class));

    }

    public List<CustomerOutstandingInvoice> getCustomerOutstandingInvoices(Long customerId) {
        String sql = "select * from outstanding_invoices_per_customer where customer_id = ?";
        return jdbcTemplate.query(sql, new DataClassRowMapper<>(CustomerOutstandingInvoice.class), customerId);
    }

    public List<CustomerAging> getCustomerAging() {
        String sql = "select * from customer_aging";

        return jdbcTemplate.query(sql, (rs, rowNum) -> new CustomerAging(
                rs.getLong("customer_id"),
                rs.getString("customer_name"),
                rs.getBigDecimal("total_outstanding"),
                rs.getBigDecimal("current_amount"),
                rs.getBigDecimal("overdue_1_30"),
                rs.getBigDecimal("overdue_31_60"),
                rs.getBigDecimal("overdue_61_90"),
                rs.getBigDecimal("overdue_90_plus")));
    }

    public List<CustomerUnallocatedCredits> getUnallocatedPaymentDrPerCustomer() {
        String sql = "select * from unallocated_payment_dr_per_customer";

        return jdbcTemplate.query(sql, new DataClassRowMapper<>(CustomerUnallocatedCredits.class));
    }

    public List<CustomerUnallocatedRefunds> getUnpostedCustomerRefunds() {
        String sql = "select * from unposted_customer_refunds";

        return jdbcTemplate.query(sql, new DataClassRowMapper<>(CustomerUnallocatedRefunds.class));
    }

}
