package com.leyou.user.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.NumberUtils;
import com.leyou.common.utils.ValidationUtils;
import com.leyou.user.mapper.UserMapper;
import com.leyou.user.pojo.User;
import com.leyou.user.utils.CodecUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author guanlibin
 * @version 1.0
 * @create 2020/9/25 21:41
 */
@Slf4j
@Service
public class UserService {

    private static final String KEY_PREFIX = "user:verify:phone:";

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public Boolean checkData(String data, Integer type) {
        User user = new User();
        // 1为用户名，2为手机号
        switch (type) {
            case 1:
                user.setUsername(data);
                break;
            case 2:
                user.setPhone(data);
                break;
            default:
                throw new LyException(ExceptionEnum.INCORRECT_PARAMETERS);
        }
        return userMapper.selectCount(user) == 0;
    }

    public void sendVerifyCode(String phone) {
        // 生成验证码
        String code = NumberUtils.generateCode(6);

        // 发送验证码
        Map<String, String> map = new HashMap<>();
        map.put("code", code);
        map.put("phone", phone);
        try {
            amqpTemplate.convertAndSend("ly.sms.exchange", "sms.verify.code", map);
        } catch (AmqpException e) {
            log.error(e.getMessage());
        }

        // 存入redis
        stringRedisTemplate.opsForValue().set(KEY_PREFIX + phone, code, 5, TimeUnit.MINUTES);
    }

    public void register(User user, String code) {
        // 1.验证code
        String cacheCode = stringRedisTemplate.opsForValue().get(KEY_PREFIX + user.getPhone());
        if (!StringUtils.equals(cacheCode, code)) {
            throw new LyException(ExceptionEnum.INVALID_VERIFY_CODE);
        }

        // 2.加密密码
        // 2.1 生成salt并记录
        String salt = CodecUtils.generateSalt();
        user.setSalt(salt);
        // 2.2 使用salt和password进行加密
        user.setPassword(CodecUtils.md5Hex(user.getPassword(), salt));

        // 3.新增用户
        user.setCreated(new Date());
        int count = userMapper.insert(user);
        if (count != 1) {
            throw new LyException(ExceptionEnum.USER_REGISTER_FAIL);
        }

    }

    public User queryUserByUsernameAndPassword(String username, String password) {
        User user = new User();
        user.setUsername(username);

        // 查询用户
        User u = userMapper.selectOne(user);

        if (u != null) {
            //校验密码
            if (!StringUtils.equals(u.getPassword(), CodecUtils.md5Hex(password, u.getSalt()))) {
                u = null;
            }
        }

        return u;
    }
}
