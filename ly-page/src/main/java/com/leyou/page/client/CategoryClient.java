package com.leyou.page.client;

import com.leyou.item.api.CategoryApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author guanlibin
 * @version 1.0
 * @create 2020/9/21 16:14
 */
@FeignClient("item-service")
public interface CategoryClient extends CategoryApi {
}
