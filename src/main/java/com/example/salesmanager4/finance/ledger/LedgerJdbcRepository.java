package com.example.salesmanager4.finance.ledger;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class LedgerJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public List<LedgerEntryRow> findEntries(LocalDate from, LocalDate to, LedgerKind kind, Long categoryId) {

        List<Object> params = new ArrayList<>();
        params.add(from);
        params.add(to);

        String sql = """
            SELECT
                le.id,
                le.entry_date,
                le.kind,
                lc.name AS category_name,
                le.description,
                le.amount,
                le.payment_method,
                s.name AS supplier_name,
                e.known_name AS recorded_by
            FROM ledger_entry le
            LEFT JOIN ledger_category lc ON lc.id = le.category_id
            LEFT JOIN supplier s ON s.supplier_id = le.supplier_id
            LEFT JOIN employee e ON e.id = le.employee_id
            WHERE le.entry_date BETWEEN ? AND ?
            """;

        if (kind != null) {
            sql += " AND le.kind = ?";
            params.add(kind.name());
        }

        if (categoryId != null) {
            sql += " AND le.category_id = ?";
            params.add(categoryId);
        }

        sql += " ORDER BY le.entry_date DESC, le.id DESC";

        return jdbcTemplate.query(sql, new DataClassRowMapper<>(LedgerEntryRow.class), params.toArray());
    }
}
