package com.leyou.item.service.impl;


import com.leyou.common.dto.CartDTO;
import com.leyou.item.service.GoodsService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

/**
 * @author guanlibin
 * @version 1.0
 * @create 2020/9/27 14:32
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class GoodsServiceImplTest {

    @Autowired
    private GoodsService goodsService;

    @Test
    public void decreaseStock() throws Exception{

        CartDTO cartDTO = new CartDTO();
        cartDTO.setNum(2);
        cartDTO.setSkuId(2600242L);

        List<CartDTO> carts = new ArrayList<>();
        carts.add(cartDTO);

        Thread t1 = new Thread(() -> goodsService.decreaseStock(carts));
        Thread t2 = new Thread(() -> goodsService.decreaseStock(carts));
        Thread t3 = new Thread(() -> goodsService.decreaseStock(carts));
        Thread t4 = new Thread(() -> goodsService.decreaseStock(carts));
        Thread t5 = new Thread(() -> goodsService.decreaseStock(carts));
        Thread t6 = new Thread(() -> goodsService.decreaseStock(carts));

        t1.start();
        t2.start();
        t3.start();
        t4.start();
        t5.start();
        t6.start();

        t1.join();
        t2.join();
        t3.join();
        t4.join();
        t5.join();
        t6.join();
    }
}