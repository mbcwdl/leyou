package com.leyou.item.service;

import com.leyou.item.pojo.Brand;
import com.leyou.common.vo.PageResult;

import java.util.List;

/**
 * @author guanlibin
 * @version 1.0
 * @create 2020/9/19 0:01
 */
public interface BrandService {
     PageResult<Brand> queryBrandByPage(Integer page, Integer rows, String key, Boolean desc, String sortBy);

     void saveBrand(Brand brand, List<Long> cids);

    Brand queryBrandById(Long bid);

    List<Brand> queryBrandByCid(Long cid);

    List<Brand> queryBrandListByIds(List<Long> ids);
}
