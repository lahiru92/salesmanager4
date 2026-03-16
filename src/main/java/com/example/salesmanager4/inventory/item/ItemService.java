package com.example.salesmanager4.inventory.item;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.salesmanager4.inventory.category.CategoryRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class ItemService {


    private final ItemRepository itemRepo;
    private final CategoryRepository categoryRepo;
    

    public ItemService(ItemRepository itemRepo,
                       CategoryRepository categoryRepo) {
        this.itemRepo = itemRepo;
        this.categoryRepo = categoryRepo;
    }

    public Item create(Item item) {

        // Validate category
        categoryRepo.findById(item.getCategoryId())
            .orElseThrow(() ->
                new IllegalArgumentException("Invalid category"));

        // Unique item code
        itemRepo.findByCode(item.getCode())
            .ifPresent(i -> {
                throw new IllegalArgumentException("Item code already exists");
            });

        // Defaults
        item.setActive(true);
        if (item.getReorderLevel() == null) {
            item.setReorderLevel(BigDecimal.ZERO);
        }

        return itemRepo.save(item);
    }

    public List<Item> findActiveItems() {
        return itemRepo.findByActiveTrueOrderByNameAsc();
    }

    public List<ItemListResponseDto> findItemList() {
        return itemRepo.findItemList();
    }

    public List<Item> findItemByName(String name) {
        return itemRepo.findByName(name);
    }

    public Item findById(Long id) {
        return itemRepo.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Item not found"));
    }

    public Item disable(Long itemId) {
        Item item = itemRepo.findById(itemId)
            .orElseThrow(() -> new IllegalArgumentException("Item not found"));

        item.setActive(false);
        return itemRepo.save(item);
    }

    public void update(Item item) {
        itemRepo.findById(item.getItemId())
            .orElseThrow(() -> new IllegalArgumentException("Item not found"));
        itemRepo.save(item);
    }

    public Page<ItemListResponseDto> listFilterdPaged(int page, int size) {
        
        long offset = (long) page * size;
        int limit = size;
        List<ItemListResponseDto> items = itemRepo.findItemListPaged(limit, offset);
        long totalItems =  itemRepo.findItemListPagedCount();
        
        Pageable pageable = PageRequest.of(page, size);
        
        return new PageImpl<>(items, pageable, totalItems);
    }
}
