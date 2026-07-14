package com.example.salesmanager4.finance.ledger;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.salesmanager4.users.CurrentUserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class LedgerService {

    private final LedgerEntryRepository entryRepository;
    private final LedgerCategoryRepository categoryRepository;
    private final CurrentUserService currentUserService;

    @Transactional
    public LedgerEntry createEntry(LedgerEntryRequest request) {

        Long employeeId = currentUserService.getEmployeeId();
        if (employeeId == null) {
            throw new RuntimeException("Employee ID not found for current user");
        }

        LedgerCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        if (!category.isActive()) {
            throw new RuntimeException("Category is disabled: " + category.getName());
        }

        LedgerEntry entry = new LedgerEntry();
        entry.setEntryDate(request.getEntryDate());
        entry.setKind(category.getKind());
        entry.setCategoryId(category.getId());
        entry.setDescription(request.getDescription());
        entry.setAmount(request.getAmount());
        entry.setPaymentMethod(request.getPaymentMethod());
        entry.setSupplierId(request.getSupplierId());
        entry.setEmployeeId(employeeId);

        log.info("Creating ledger entry: {}", entry);

        return entryRepository.save(entry);
    }

    public void deleteEntry(Long id) {
        entryRepository.deleteById(id);
    }

    public LedgerCategory createCategory(String name, LedgerKind kind) {
        LedgerCategory category = new LedgerCategory();
        category.setName(name);
        category.setKind(kind);
        category.setActive(true);
        return categoryRepository.save(category);
    }

    public void disableCategory(Long id) {
        LedgerCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        category.setActive(false);
        categoryRepository.save(category);
    }
}
