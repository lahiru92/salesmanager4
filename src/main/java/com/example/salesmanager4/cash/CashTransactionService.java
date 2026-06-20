package com.example.salesmanager4.cash;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CashTransactionService {

    private final CashTransactionRepository repo;

    public void postCashTransaction(CashTransaction tran) {
        repo.save(tran);
    }
}
