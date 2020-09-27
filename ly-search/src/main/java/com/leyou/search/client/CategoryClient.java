package com.leyou.search.client;

import com.leyou.item.api.CategoryApi;
import com.leyou.item.pojo.Category;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author guanlibin
 * @version 1.0
 * @create 2020/9/21 16:14
 */
@FeignClient("item-service")
public interface CategoryClient extends CategoryApi {
}
