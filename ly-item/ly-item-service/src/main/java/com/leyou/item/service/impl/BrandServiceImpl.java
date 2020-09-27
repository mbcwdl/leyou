package com.leyou.item.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.item.mapper.BrandMapper;
import com.leyou.item.pojo.Brand;
import com.leyou.common.vo.PageResult;
import com.leyou.item.service.BrandService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

/**
 * @author guanlibin
 * @version 1.0
 * @create 2020/9/19 0:01
 */
@Slf4j
@Service
public class BrandServiceImpl implements BrandService {
    @Autowired
    private BrandMapper brandMapper;

    @Override
    public PageResult<Brand> queryBrandByPage(Integer page, Integer rows, String key, Boolean desc, String sortBy) {
        // 分页
        PageHelper.startPage(page, rows);
        Example example = new Example(Brand.class);
        Example.Criteria criteria = example.createCriteria();
        // 过滤
        if (StringUtils.isNotBlank(key)) {
            criteria.orLike("name", "%" + key + "%")
                    .orEqualTo("letter", key.toUpperCase());
        }
        // 排序
        if (StringUtils.isNotBlank(sortBy)) {
            example.setOrderByClause(sortBy + (desc ? " DESC" : ""));
        }
        // 查询
        List<Brand> list = brandMapper.selectByExample(example);
        // 解析分页结果
        PageInfo<Brand> info = new PageInfo<>(list);

        return new PageResult<Brand>(info.getTotal(), info.getList());
    }

    @Override
    @Transactional
    public void saveBrand(Brand brand, List<Long> cids) {
        log.info("品牌名是：" + brand.getName());
        brand.setId(null);
        int affectRow = brandMapper.insert(brand);
        if (affectRow != 1) {
            throw new LyException(ExceptionEnum.BRAND_SAVE_ERROR);
        }

        Long bid = brand.getId();
        for (Long cid : cids) {
            affectRow = brandMapper.insertCategoryBrand(cid, bid);
            if (affectRow != 1) {
                throw new LyException(ExceptionEnum.BRAND_SAVE_ERROR);
            }
        }
    }

    @Override
    public Brand queryBrandById(Long bid) {
        Brand brand = brandMapper.selectByPrimaryKey(bid);
        if (brand == null) {
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        return brand;
    }

    @Override
    public List<Brand> queryBrandByCid(Long cid) {
        List<Brand> list = brandMapper.queryBrandByCid(cid);
        if (CollectionUtils.isEmpty(list)) {
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        return list;
    }

    @Override
    public List<Brand> queryBrandListByIds(List<Long> ids) {
        List<Brand> brandList = brandMapper.selectByIdList(ids);
        if (CollectionUtils.isEmpty(brandList)) {
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        return brandList;
    }
}
