package com.leyou.search.client;

import com.leyou.item.pojo.Brand;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

/**
 * @author guanlibin
 * @version 1.0
 * @create 2020/9/21 16:50
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class BrandClientTest {
    @Autowired
    private BrandClient brandClient;

    @Test
    public void queryBrandById() {
        Brand b = brandClient.queryBrandById(3539L);
        System.out.println(b);
    }

}