package com.example.salesmanager4.cash.balancing;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

public interface CashHandoverRepository extends CrudRepository<CashHandover, Long> {

    List<CashHandover> findByHandoverDate(LocalDate handoverDate);

    Optional<CashHandover> findByEmployeeIdAndHandoverDate(Long employeeId, LocalDate handoverDate);
}
