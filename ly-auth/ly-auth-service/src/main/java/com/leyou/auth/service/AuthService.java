package com.leyou.auth.service;

import com.leyou.auth.client.UserClient;
import com.leyou.auth.config.JwtProperties;
import com.leyou.auth.payload.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.user.pojo.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

/**
 * @author guanlibin
 * @version 1.0
 * @create 2020/9/26 9:56
 */
@Slf4j
@Service
@EnableConfigurationProperties(JwtProperties.class)
public class AuthService {

    @Autowired
    private UserClient userClient;

    @Autowired
    private JwtProperties properties;

    public String login(String username, String password) {
        // 查询用户信息
        User user = userClient.queryUserByUsernameAndPassword(username, password);
        if (user == null) {
            throw new LyException(ExceptionEnum.USERNAME_OR_PASSWORD_ERROR);
        }
        // 生成token
        try {
            return JwtUtils.generateToken(new UserInfo(user.getId(),user.getUsername()),
                    properties.getPrivateKey(),properties.getExpire() );
        } catch (Exception e) {
            throw new LyException(ExceptionEnum.CREATE_TOKEN_ERROR);
        }

    }

}
