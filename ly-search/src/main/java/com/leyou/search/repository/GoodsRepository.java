package com.leyou.search.repository;

import com.leyou.search.pojo.Goods;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * @author guanlibin
 * @version 1.0
 * @create 2020/9/21 17:09
 */
public interface GoodsRepository extends ElasticsearchRepository<Goods, Long> {
}
