package com.leyou.search.client;

import com.leyou.item.api.BrandApi;
import com.leyou.item.pojo.Brand;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * @author guanlibin
 * @version 1.0
 * @create 2020/9/21 16:49
 */
@FeignClient("item-service")
public interface BrandClient extends BrandApi {
}
