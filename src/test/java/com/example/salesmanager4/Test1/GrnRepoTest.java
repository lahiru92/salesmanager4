package com.example.salesmanager4.Test1;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.salesmanager4.grn.GrnRepository;

@SpringBootTest
public class GrnRepoTest {

    @Autowired
    GrnRepository grnRepo;

    @Test
    public void testGrnRequestDto() {
        System.out.println(grnRepo.findRequestDtoById(3L));
    }
}
