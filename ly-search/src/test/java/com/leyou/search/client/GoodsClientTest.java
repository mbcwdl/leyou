package com.leyou.search.client;

import com.leyou.item.pojo.Sku;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.junit.Assert.*;

/**
 * @author guanlibin
 * @version 1.0
 * @create 2020/9/21 16:53
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class GoodsClientTest {
    @Autowired
    private GoodsClient goodsClient;

    @Test
    public void querySpuDetailBySpuId() {
        List<Sku> skus = goodsClient.querySkuBySpuId(2L);
        for (Sku sku : skus) {
            System.out.println(sku);
        }
    }

    @Test
    public void querySkuBySpuId() {

    }

    @Test
    public void querySpuByPage() {

    }


}