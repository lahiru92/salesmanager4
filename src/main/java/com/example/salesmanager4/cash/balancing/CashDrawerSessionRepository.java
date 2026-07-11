package com.example.salesmanager4.cash.balancing;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

public interface CashDrawerSessionRepository extends CrudRepository<CashDrawerSession, Long> {

    Optional<CashDrawerSession> findBySessionDate(LocalDate sessionDate);

    @Query("""
        SELECT * FROM cash_drawer_session
        WHERE session_date < :sessionDate
        ORDER BY session_date DESC
        LIMIT 1
        """)
    Optional<CashDrawerSession> findLatestBefore(LocalDate sessionDate);
}
