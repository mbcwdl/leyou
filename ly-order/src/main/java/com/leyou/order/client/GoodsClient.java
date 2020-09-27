package com.leyou.order.client;

import com.leyou.item.api.GoodsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author guanlibin
 * @version 1.0
 * @create 2020/9/21 16:49
 */
@FeignClient("item-service")
public interface GoodsClient extends GoodsApi {
}
