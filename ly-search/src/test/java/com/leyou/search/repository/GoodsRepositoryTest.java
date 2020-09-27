package com.leyou.search.repository;

import com.leyou.search.pojo.Goods;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

/**
 * @author guanlibin
 * @version 1.0
 * @create 2020/9/21 17:09
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class GoodsRepositoryTest {
    @Autowired
    private ElasticsearchTemplate template;

    @Autowired
    private GoodsRepository repository;

    @Test
    public void createIndex() {
        template.createIndex(Goods.class);
        template.putMapping(Goods.class);

    }
}