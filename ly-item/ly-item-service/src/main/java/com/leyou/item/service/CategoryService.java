package com.leyou.item.service;

import com.leyou.item.pojo.Category;

import java.util.List;

/**
 * @author guanlibin
 * @version 1.0
 * @create 2020/9/18 22:15
 */
public interface CategoryService {
    List<Category> queryCategoryListByPid(Long pid);

    List<Category> queryByIds(List<Long> ids);
}
