package com.example.salesmanager4.inventory.category;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

public interface CategoryRepository extends CrudRepository<Category, Long> {

    Optional<Category> findByNormalizedName(String normalizedName);

    List<Category> findAllByOrderByNameAsc();
}
