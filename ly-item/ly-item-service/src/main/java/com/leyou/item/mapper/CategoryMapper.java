package com.leyou.item.mapper;

import com.leyou.item.pojo.Category;
import tk.mybatis.mapper.additional.idlist.IdListMapper;
import tk.mybatis.mapper.common.Mapper;

/**
 * @author guanlibin
 * @version 1.0
 * @create 2020/9/18 22:14
 */
public interface CategoryMapper extends Mapper<Category>, IdListMapper<Category, Long> {
}
