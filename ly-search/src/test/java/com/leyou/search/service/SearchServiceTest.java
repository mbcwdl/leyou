package com.leyou.search.service;

import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.Spu;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.repository.GoodsRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * @author guanlibin
 * @version 1.0
 * @create 2020/9/21 19:36
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class SearchServiceTest {
    @Autowired
    private SearchService searchService;
    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private GoodsRepository goodsRepository;
    @Autowired
    private ElasticsearchTemplate template;

    @Test
    public void buildGoods() {
        int page = 1;
        int rows = 100;
        int size = 100;
        do {
            // 分页查询spu
            PageResult<Spu> spuPageResult = goodsClient.querySpuByPage(page++, rows, null, true);
            // 获取spu集合
            List<Spu> spuList = spuPageResult.getItems();
            size = spuList.size();
            // 构建Goods集合
            List<Goods> goodsList = spuList.stream().map(searchService::buildGoods).collect(Collectors.toList());
            // 加入到索引库
            goodsRepository.saveAll(goodsList);

        } while (size == 100);

    }
    @Test
    public void createIndex() {
        template.createIndex(Goods.class);
        template.putMapping(Goods.class);
    }
}