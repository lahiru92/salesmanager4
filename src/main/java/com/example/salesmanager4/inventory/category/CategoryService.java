package com.example.salesmanager4.inventory.category;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CategoryService {

    private final CategoryRepository repo;

    public CategoryService(CategoryRepository repo) {
        this.repo = repo;
    }

    public Category create(String name) {
        String normalized = normalize(name);

        repo.findByNormalizedName(normalized)
            .ifPresent(c -> {
                throw new IllegalArgumentException("Category already exists");
            });

        Category category = new Category();
        category.setName(name.trim());
        category.setNormalizedName(normalized);

        return repo.save(category);
    }

    public List<Category> findAll() {
        return repo.findAllByOrderByNameAsc();
    }

    private String normalize(String value) {
        return value == null ? null : value.trim().toLowerCase();
    }
}
