package com.example.salesmanager4.grn;

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
public class GrnJdbcRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public Page<GrnListResponseDto> findAllByPage(GrnListRequestDto requestDto, Pageable pageable) {
        
        // select
        //     g.id,
        //     g.purchase_order_id AS purchaseOrderId,
        //     g.status,
        //     g.received_date AS receivedDate,
        //     s.name AS supplierName,
        //     e.known_name AS employeeName
        // from grn g
        // left join supplier s on g.supplier_id = s.supplier_id
        // left join employee e on g.employee_id = e.id;

        Map<String, Object> parameters = new HashMap<>();

        String querySelection = """
                	g.id,
                    g.purchase_order_id AS purchaseOrderId,
                    g.status,
                    g.received_date AS receivedDate,
                    s.name AS supplierName,
                    e.known_name AS employeeName
                """;
        String countSelection = "COUNT(*)";

        String query = """
                FROM grn g
                LEFT JOIN supplier s on g.supplier_id = s.supplier_id
                LEFT JOIN employee e on g.employee_id = e.id
                """;

        String whereClause = "WHERE 1=1";

        if (requestDto.getStatus() != null && !requestDto.getStatus().isEmpty()) {
            whereClause += " AND g.status = :status";
            parameters.put("status", requestDto.getStatus());
        }

        if (requestDto.getFromReceivedDate() != null) {
            whereClause += " AND g.received_date >= :fromReceivedDate";
            parameters.put("fromReceivedDate", requestDto.getFromReceivedDate());
        }

        if (requestDto.getToReceivedDate() != null) {
            whereClause += " AND g.received_date <= :toReceivedDate";
            parameters.put("toReceivedDate", requestDto.getToReceivedDate());
        }

        if (requestDto.getSupplierId() != null) {
            whereClause += " AND g.supplier_id = :supplierId";
            parameters.put("supplierId", requestDto.getSupplierId());
        }

        if (requestDto.getEmployeeId() != null) {
            whereClause += " AND g.employee_id = :employeeId";
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
        String countQuery = "SELECT " + countSelection + " " + query + " " + whereClause;

        List<GrnListResponseDto> content = namedParameterJdbcTemplate.query(sql, parameters, new BeanPropertyRowMapper<>(GrnListResponseDto.class));
        Long total = namedParameterJdbcTemplate.queryForObject(countQuery, parameters, Long.class);
        if (total == null) {
            total = 0L;
        }
        return new PageImpl<>(content, pageable, total);
    }
}
