package com.leyou.search.client;

import com.leyou.item.api.SpecificationApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author guanlibin
 * @version 1.0
 * @create 2020/9/21 16:48
 */
@FeignClient("item-service")
public interface SpecificationClient extends SpecificationApi {
}
