package com.leyou.search.pojo;

import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.Brand;
import com.leyou.item.pojo.Category;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * @author guanlibin
 * @version 1.0
 * @create 2020/9/22 16:58
 */
@Data
@NoArgsConstructor
public class SearchResult extends PageResult<Goods> {

    private List<Brand> brands;
    private List<Category> categories;
    private List<Map<String, Object>> specs;

    public SearchResult(Long total, Integer totalPage, List items, List<Brand> brands,
                        List<Category> categories, List<Map<String, Object>> specs) {
        super(total, totalPage, items);
        this.brands = brands;
        this.categories = categories;
        this.specs = specs;
    }

    public static SearchResult build(Long total, Integer totalPage, List items, List<Brand> brands,
                                     List<Category> categories, List<Map<String, Object>> specs) {
        return new SearchResult(total, totalPage, items, brands, categories, specs);
    }
}
