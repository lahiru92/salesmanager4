package com.example.salesmanager4.Test1;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.example.salesmanager4.grn.GrnJdbcRepository;
import com.example.salesmanager4.grn.GrnListRequestDto;
import com.example.salesmanager4.grn.GrnListResponseDto;

@SpringBootTest
public class GrnJdbcRepoTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Test
    public void test1() {
        GrnJdbcRepository grnJdbcRepo = new GrnJdbcRepository(namedParameterJdbcTemplate);

        GrnListRequestDto requestDto = new GrnListRequestDto();
        requestDto.setStatus("APPROVED");

        Pageable pageable = PageRequest.of(0, 10, 
            Sort.by("receivedDate").descending()   
        );
        Page<GrnListResponseDto> page = grnJdbcRepo.findAllByPage(requestDto, pageable);
        System.out.println("\nPage content: " + page.getContent());
    }
}
