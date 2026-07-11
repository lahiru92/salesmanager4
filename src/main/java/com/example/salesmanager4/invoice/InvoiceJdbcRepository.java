package com.example.salesmanager4.invoice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class InvoiceJdbcRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public Page<InvoiceListResponseDto> findAllByPage(InvoiceListRequestDto requestDto, Pageable pageable) {

        Map<String, Object> parameters = new HashMap<>();

        String querySelection = """
                	i.id,
                    i.status,
                    i.invoice_date AS invoiceDate,
                    c.name AS customerName,
                    e.known_name AS employeeName,
                    i.cash,
                    i.cheque,
                    i.credit,
                    i.total
                """;

        String query = """
                FROM invoice i
                LEFT JOIN customer c on i.customer_id = c.customer_id
                LEFT JOIN employee e on i.employee_id = e.id
                """;

        String whereClause = "WHERE 1=1";



        if (requestDto.getStatus() != null && !requestDto.getStatus().isEmpty()) {
            whereClause += " AND i.status = :status";
            parameters.put("status", requestDto.getStatus());
        }

        if (requestDto.getFromInvoiceDate() != null) {
            whereClause += " AND i.invoice_date >= :fromInvoiceDate";
            parameters.put("fromInvoiceDate", requestDto.getFromInvoiceDate());
        }

        if (requestDto.getToInvoiceDate() != null) {
            whereClause += " AND i.invoice_date <= :toInvoiceDate";
            parameters.put("toInvoiceDate", requestDto.getToInvoiceDate());
        }

        if (requestDto.getCustomerId() != null) {
            whereClause += " AND i.customer_id = :customerId";
            parameters.put("customerId", requestDto.getCustomerId());
        }

        if (requestDto.getEmployeeId() != null) {
            whereClause += " AND i.employee_id = :employeeId";
            parameters.put("employeeId", requestDto.getEmployeeId());
        }

        String orderByClause = "";

        if (pageable.getSort().isSorted()) {
            orderByClause += " ORDER BY ";
            String orderClause = pageable.getSort().stream()
                .map(order -> order.getProperty() + " " + order.getDirection().name())
                .collect(Collectors.joining(", "));
            orderByClause += orderClause;
        }


        String sql = "SELECT " + querySelection + " " + query + " " + whereClause + " " + orderByClause + " LIMIT " + pageable.getPageSize() + " OFFSET " + pageable.getOffset();
        String countQuery = "SELECT COUNT(*) FROM invoice i " + whereClause;

        List<InvoiceListResponseDto> content = namedParameterJdbcTemplate.query(sql, parameters, new BeanPropertyRowMapper<>(InvoiceListResponseDto.class));
        Long total = namedParameterJdbcTemplate.queryForObject(countQuery, parameters, Long.class);
        if (total == null) {
            total = 0L;
        }
        return new PageImpl<>(content, pageable, total);
    }
}
