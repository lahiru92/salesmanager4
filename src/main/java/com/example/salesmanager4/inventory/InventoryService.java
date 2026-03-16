package com.example.salesmanager4.inventory;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class InventoryService {

    private final InventoryRepository repo;

    public InventoryService(InventoryRepository repo) {
        this.repo = repo;
    }

    public List<InventorySummaryView> getInventorySummary() {
        return repo.findInventorySummary();
    }
}

