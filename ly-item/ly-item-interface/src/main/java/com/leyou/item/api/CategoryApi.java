package com.leyou.item.api;

import com.leyou.item.pojo.Category;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author guanlibin
 * @version 1.0
 * @create 2020/9/21 16:39
 */
public interface CategoryApi {
    @GetMapping("/category/list/ids")
    List<Category> queryCategoryListByIds(@RequestParam("ids") List<Long> ids);
}
