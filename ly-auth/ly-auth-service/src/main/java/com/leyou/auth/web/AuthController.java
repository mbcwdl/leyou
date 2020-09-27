package com.leyou.auth.web;

import com.leyou.auth.config.JwtProperties;
import com.leyou.auth.payload.UserInfo;
import com.leyou.auth.service.AuthService;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.CookieUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author guanlibin
 * @version 1.0
 * @create 2020/9/26 9:56
 */
@RestController
@EnableConfigurationProperties(JwtProperties.class)
public class AuthController {

    @Autowired
    private AuthService authService;

    @Value("${ly.jwt.cookieName}")
    private String cookieName;

    @Autowired
    private JwtProperties properties;

    @PostMapping("login")
    public ResponseEntity<Void> login(@RequestParam("username") String username,
                                      @RequestParam("password") String password,
                                      HttpServletRequest req, HttpServletResponse resp) {
        // 鉴权后返回token
        String token = authService.login(username, password);
        // 写入cookie
        CookieUtils.newBuilder(resp).httpOnly().request(req).build(cookieName, token);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("verify")
    public ResponseEntity<UserInfo> verify(HttpServletRequest req, HttpServletResponse resp) {
        try {
            // 解析token拿到UserInfo
            UserInfo userInfo = JwtUtils.getInfoFromToken(CookieUtils.
                    getCookieValue(req, cookieName), properties.getPublicKey());

            // 刷新jwt失效时间
            String token = JwtUtils.generateToken(userInfo, properties.getPrivateKey(), properties.getExpire());

            // 写入cookie
            CookieUtils.newBuilder(resp).httpOnly().request(req).build(cookieName, token);

            return ResponseEntity.ok(userInfo);
        } catch (Exception e) {
            throw new LyException(ExceptionEnum.NOT_AUTHORIZED);
        }

    }
}
