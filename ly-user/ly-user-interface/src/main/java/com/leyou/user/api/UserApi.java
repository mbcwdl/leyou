package com.leyou.user.api;

import com.leyou.user.pojo.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author guanlibin
 * @version 1.0
 * @create 2020/9/26 10:07
 */
public interface UserApi {


    @GetMapping("query")
    User queryUserByUsernameAndPassword(@RequestParam("username") String username,
                                        @RequestParam("password") String password);
}