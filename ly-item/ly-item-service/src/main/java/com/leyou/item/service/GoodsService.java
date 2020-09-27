package com.leyou.item.service;

import com.leyou.common.dto.CartDTO;
import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;

import java.util.List;

/**
 * @author guanlibin
 * @version 1.0
 * @create 2020/9/20 13:06
 */
public interface GoodsService {
    PageResult<Spu> querySpuByPage(Integer page, Integer rows, Boolean saleable, String key);

    void saveGoods(Spu spu);

    SpuDetail querySpuDetailBySpuId(Long spuId);

    List<Sku> querySkusBySpuId(Long spuId);

    void updateGoods(Spu spu);

    Spu querySpuById(Long spuId);

    List<Sku> querySkuByIds(List<Long> ids);

    void decreaseStock(List<CartDTO> cartDTOList);
}
