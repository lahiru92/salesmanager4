package com.example.salesmanager4.Test1;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import com.example.salesmanager4.inventory.item.ItemListResponseDto;
import com.example.salesmanager4.inventory.item.ItemRepository;
import com.example.salesmanager4.inventory.item.ItemService;

@SpringBootTest
public class ItemListTest {

    @Autowired
    ItemRepository itemRepo;

    @Autowired
    ItemService itemService;

    @Test
    public void testItemList() {
        List<ItemListResponseDto> items = itemRepo.findItemListPaged(5, 0);
        items.forEach(System.out::println);

        System.out.println("Total count: " + itemRepo.findItemListPagedCount());
    }

    @Test
    public void testItemListPaged() {
        Page<ItemListResponseDto>[] pages = new Page[5];
        pages[0] = itemService.listFilterdPaged(0,5);
        pages[1] = itemService.listFilterdPaged(1,5);
        pages[2] = itemService.listFilterdPaged(2,5);
        pages[3] = itemService.listFilterdPaged(3,5);
        pages[4] = itemService.listFilterdPaged(4,5);

        for (Page<ItemListResponseDto> page : pages) {
            System.out.println("================================ ");
            System.out.println("Total count: " + page.getTotalElements());
            System.out.println("Total pages: " + page.getTotalPages());
            System.out.println("Current page: " + page.getNumber());
            System.out.println("Page size: " + page.getSize());
            System.out.println("Number of elements in current page: " + page.getNumberOfElements());
            System.out.println("Is first page: " + page.isFirst());
            System.out.println("Is last page: " + page.isLast());
            System.out.println("Has next page: " + page.hasNext());
            System.out.println("Has previous page: " + page.hasPrevious());
            System.out.println("Next page number: " + page.nextOrLastPageable().getPageNumber());
            System.out.println("Previous page number: " + page.previousOrFirstPageable().getPageNumber());
            System.out.println("Sort: " + page.getSort());
            System.out.println("Pageable: " + page.getPageable());
            System.out.println("Is empty: " + page.isEmpty());
        }


    }
}