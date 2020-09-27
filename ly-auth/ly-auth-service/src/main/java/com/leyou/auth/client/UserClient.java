package com.leyou.auth.client;

import com.leyou.user.api.UserApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author guanlibin
 * @version 1.0
 * @create 2020/9/26 10:10
 */
@FeignClient("user-service")
public interface UserClient extends UserApi {
}
