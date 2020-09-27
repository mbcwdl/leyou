package com.leyou.sms;

import com.leyou.common.utils.JsonUtils;
import com.leyou.sms.utils.SmsUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author guanlibin
 * @version 1.0
 * @create 2020/9/25 18:33
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class LySmsApplicationTest {

    @Autowired
    private AmqpTemplate template;

    @Test
    public void testSms() {
        Map<String, String> map = new HashMap<>();
        map.put("code", "1234");
        map.put("phone", "15252062654");
        template.convertAndSend("ly.sms.exchange", "sms.verify.code", map);
    }
}