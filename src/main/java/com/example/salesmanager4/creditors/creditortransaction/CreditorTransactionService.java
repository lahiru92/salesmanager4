package com.example.salesmanager4.creditors.creditortransaction;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreditorTransactionService {

    private final CreditorTransactionRepository repo;

    public void postPayable(CreditorTransaction txn) {
        repo.save(txn);
    }
}
