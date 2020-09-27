package com.leyou.user.web;

import com.leyou.user.pojo.User;
import com.leyou.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @author guanlibin
 * @version 1.0
 * @create 2020/9/25 21:40
 */
@RestController
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户名或手机号唯一性检查
     * @param data 用户名或者手机号
     * @param type 1为用户名，2为手机号
     * @return 是否唯一
     */
    @GetMapping("/check/{data}/{type}")
    public ResponseEntity<Boolean> checkData(@PathVariable("data") String data, @PathVariable("type") Integer type) {
        return ResponseEntity.ok(userService.checkData(data, type));
    }

    /**
     * 发送短信验证码
     * @param phone
     * @return
     */
    @PostMapping("code")
    public ResponseEntity<Void> sendVerifyCode(@RequestParam("phone") String phone) {
        userService.sendVerifyCode(phone);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("register")
    public ResponseEntity<Void> register(User user, @RequestParam("code") String code) {
        userService.register(user, code);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("query")
    public ResponseEntity<User> queryUserByUsernameAndPassword(@RequestParam("username") String username,
                                                               @RequestParam("password") String password) {
        return ResponseEntity.ok(userService.queryUserByUsernameAndPassword(username, password));
    }

}
