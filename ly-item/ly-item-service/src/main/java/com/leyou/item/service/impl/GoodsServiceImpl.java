package com.leyou.item.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.dto.CartDTO;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.vo.PageResult;
import com.leyou.item.mapper.SkuMapper;
import com.leyou.item.mapper.SpuDetailMapper;
import com.leyou.item.mapper.SpuMapper;
import com.leyou.item.mapper.StockMapper;
import com.leyou.item.pojo.*;
import com.leyou.item.service.BrandService;
import com.leyou.item.service.CategoryService;
import com.leyou.item.service.GoodsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import javax.xml.crypto.Data;
import java.awt.dnd.DropTarget;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author guanlibin
 * @version 1.0
 * @create 2020/9/20 13:18
 */
@Slf4j
@Service
public class GoodsServiceImpl implements GoodsService {

    @Autowired
    private SpuMapper spuMapper;

    @Autowired
    private SpuDetailMapper spuDetailMapper;

    @Autowired
    private BrandService brandService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private StockMapper stockMapper;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Override
    public PageResult<Spu> querySpuByPage(Integer page, Integer rows, Boolean saleable, String key) {
        // 分页
        PageHelper.startPage(page, rows);
        // 过滤
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
          // 上下架过滤
        if (saleable != null) {
            criteria.andEqualTo("saleable", saleable);
        }
          // 搜索字段过滤
        if (StringUtils.isNotBlank(key)) {
            criteria.andLike("title", "%" + key + "%");
        }
        // 默认排序
        example.setOrderByClause("last_update_time DESC");
        // 执行查询
        List<Spu> list = spuMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(list)) {
            throw new LyException(ExceptionEnum.SPU_NOT_FOUND);
        }
        // 获取品牌名称和分类名称
        loadCategoryAndBrand(list);
        // 解析分页结果
        PageInfo<Spu> info = new PageInfo<>(list);
        return new PageResult<Spu>(info.getTotal(), list);
    }

    private void loadCategoryAndBrand(List<Spu> list) {
        for (Spu spu : list) {
            // 设置spu的分类名称
            List<String> cnames = categoryService.queryByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()))
                    .stream().map(Category::getName).collect(Collectors.toList());
            spu.setCname(StringUtils.join(cnames, "/"));
            // 设置spu的品牌名称
            spu.setBname(brandService.queryBrandById(spu.getBrandId()).getName());
        }
    }

    @Override
    @Transactional
    public void saveGoods(Spu spu) {
        // 新增spu
        spu.setId(null);
        spu.setSaleable(true);
        spu.setValid(false);
        spu.setCreateTime(new Date());
        spu.setLastUpdateTime(spu.getCreateTime());

        int count = spuMapper.insert(spu);
        if (count != 1) {
            throw new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
        }

        // 新增SpuDetail
        SpuDetail spuDetail = spu.getSpuDetail();
        spuDetail.setSpuId(spu.getId());
        count = spuDetailMapper.insert(spuDetail);
        if (count != 1) {
            throw new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
        }
        // 新增sku和库存
        saveSkuAndStock(spu);

        // 发送消息到mq
        try {
            amqpTemplate.convertAndSend("item.insert", spu.getId());
        } catch (AmqpException e) {
            log.error("消息(routingKey:item.insert,message:" + spu.getId() + "):发送失败！", e);
        }
    }

    private void saveSkuAndStock(Spu spu) {
        int count;
        // 新增sku
        List<Sku> skus = spu.getSkus();
        // 存储stock对象
        List<Stock> stocks = new ArrayList<>();
        for (Sku sku : skus) {
            sku.setCreateTime(new Date());
            sku.setLastUpdateTime(sku.getCreateTime());
            sku.setSpuId(spu.getId());

            count = skuMapper.insert(sku);
            if (count != 1) {
                throw new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
            }

            Stock stock = new Stock();
            stock.setStock(sku.getStock());
            stock.setSkuId(sku.getId());

            stocks.add(stock);
        }
        // 批量新增stock对象
        count = stockMapper.insertList(stocks);
        if (count != stocks.size()) {
            throw new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
        }
    }

    @Override
    public SpuDetail querySpuDetailBySpuId(Long spuId) {
        SpuDetail spuDetail = spuDetailMapper.selectByPrimaryKey(spuId);
        if (spuDetail == null) {
            throw new LyException(ExceptionEnum.SPU_DETAIL_NOT_FOUND);
        }
        return spuDetail;
    }

    @Override
    public List<Sku> querySkusBySpuId(Long spuId) {
        // 查询sku表
        Sku sku = new Sku();
        sku.setSpuId(spuId);

        List<Sku> skuList = skuMapper.select(sku);
        if (CollectionUtils.isEmpty(skuList)) {
            throw new LyException(ExceptionEnum.SKU_NOT_FOUND);
        }
        List<Long> ids = skuList.stream().map(Sku::getId).collect(Collectors.toList());
        loadSkuStoack(skuList, ids);

        return skuList;
    }

    private void loadSkuStoack(List<Sku> skuList, List<Long> ids) {
        // 查库存
        List<Stock> stockList = stockMapper.selectByIdList(ids);
        if (CollectionUtils.isEmpty(stockList) || stockList.size() != ids.size()) {
            throw new LyException(ExceptionEnum.STOCK_NOT_FOUND);
        }
        Map<Long, Integer> stockMap = stockList.stream()
                .collect(Collectors.toMap(Stock::getSkuId, Stock::getStock));
        skuList.forEach(s -> s.setStock(stockMap.get(s.getId())));
    }

    @Override
    @Transactional
    public void updateGoods(Spu spu) {
        // 查询sku
        Sku sku = new Sku();
        sku.setSpuId(spu.getId());

        List<Sku> skuList = skuMapper.select(sku);
        // 删除sku及库存
        if (!CollectionUtils.isEmpty(skuList)) {
            // 删除sku
            skuMapper.delete(sku);
            // 删除库存
            List<Long> skuIdList = skuList.stream().map(Sku::getId).collect(Collectors.toList());
            stockMapper.deleteByIdList(skuIdList);
        }

        // 修改spu
        spu.setLastUpdateTime(new Date());
        spu.setValid(null);
        spu.setCreateTime(null);
        spu.setSaleable(null);

        int count = spuMapper.updateByPrimaryKeySelective(spu);
        if (count != 1) {
            throw new LyException(ExceptionEnum.SPU_UPDATE_EXCEPTION);
        }
        // 修改spu detail
        spuDetailMapper.updateByPrimaryKeySelective(spu.getSpuDetail());

        // 新增sku和stock
        saveSkuAndStock(spu);

        // 发送消息到mq
        try {
            amqpTemplate.convertAndSend("item.update", spu.getId());
        } catch (AmqpException e) {
            log.error("消息(routingKey:item.update,message:" + spu.getId() + "):发送失败！", e);
        }
    }

    @Override
    public Spu querySpuById(Long spuId) {
        Spu spu = spuMapper.selectByPrimaryKey(spuId);
        if (spu == null) {
            throw new LyException(ExceptionEnum.SPU_NOT_FOUND);
        }
        // 查spuDetail
        spu.setSpuDetail(querySpuDetailBySpuId(spuId));
        // 查sku
        spu.setSkus(querySkusBySpuId(spuId));

        return spu;
    }

    @Override
    public List<Sku> querySkuByIds(List<Long> ids) {
        // 批量查询sku
        List<Sku> skus = skuMapper.selectByIdList(ids);
        if (CollectionUtils.isEmpty(skus)) {
            throw new LyException(ExceptionEnum.SKU_NOT_FOUND);
        }
        // 查库存
        loadSkuStoack(skus, ids);

        return skus;
    }

    @Override
    @Transactional
    public void decreaseStock(List<CartDTO> cartDTOList) {
        for (CartDTO cartDTO : cartDTOList) {
            int count = stockMapper.decreaseStock(cartDTO.getNum(), cartDTO.getSkuId());
            if (count != 1) {
                throw new LyException(ExceptionEnum.SKU_NOT_FOUND);
            }
        }
    }
}
