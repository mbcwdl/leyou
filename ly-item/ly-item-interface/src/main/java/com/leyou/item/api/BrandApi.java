package com.leyou.item.api;

import com.leyou.item.pojo.Brand;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author guanlibin
 * @version 1.0
 * @create 2020/9/21 16:42
 */
public interface BrandApi {

    @GetMapping("/brand/{id}")
    Brand queryBrandById(@PathVariable("id") Long id);

    @GetMapping("/brand/ids")
    List<Brand> queryBrandListByIds(@RequestParam("ids") List<Long> ids);

}
