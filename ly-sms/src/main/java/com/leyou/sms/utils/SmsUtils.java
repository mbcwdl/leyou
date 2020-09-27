package com.leyou.sms.utils;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.leyou.sms.config.SmsProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @author guanlibin
 * @version 1.0
 * @create 2020/9/25 18:21
 */
@Slf4j
@Component
@EnableConfigurationProperties(SmsProperties.class)
public class SmsUtils {

    private static final String PRODUCT = "Dysmsapi";
    private static final String DOMAIN = "dysmsapi.aliyuncs.com";

    private static final String KEY_PREFIX = "sms:phone:";

    @Autowired
    private SmsProperties properties;

    @Autowired
    private StringRedisTemplate redisTemplate;

    public SendSmsResponse sendSms(String phoneNumber, String signName, String templateCode, String templateParam) {
        String key = KEY_PREFIX + phoneNumber;

        // 1分钟内不能发送短信
        String v = redisTemplate.opsForValue().get(key);
        if (v != null) {
            log.info("[短信服务]发送验证码失败，两次发送时间间隔不能小于1分钟，手机号：{}", phoneNumber);
            return null;
        }
        try {
            //可自助调整超时时间
            System.setProperty("sun.net.client.defaultConnectTimeout", "10000");
            System.setProperty("sun.net.client.defaultReadTimeout", "10000");

            //初始化acsClient,暂不支持region化
            IClientProfile profile = DefaultProfile.getProfile("cn-hangzhou", properties.getAccessKeyId(), properties.getAccessKeySecret());
            DefaultProfile.addEndpoint("cn-hangzhou", "cn-hangzhou", PRODUCT, DOMAIN);
            IAcsClient acsClient = new DefaultAcsClient(profile);

            //组装请求对象-具体描述见控制台-文档部分内容
            SendSmsRequest request = new SendSmsRequest();
            request.setMethod(MethodType.POST);
            //必填:待发送手机号
            request.setPhoneNumbers(phoneNumber);
            //必填:短信签名-可在短信控制台中找到
            request.setSignName(signName);
            //必填:短信模板-可在短信控制台中找到
            request.setTemplateCode(templateCode);
            //可选:模板中的变量替换JSON串,如模板内容为"亲爱的${name},您的验证码为${code}"时,此处的值为
            request.setTemplateParam(templateParam);

            SendSmsResponse resp = acsClient.getAcsResponse(request);

            if (!"OK".equals(resp.getCode())) {
                log.info("[短信服务]发送验证码失败，手机号：{},原因：{}", phoneNumber, resp.getMessage());
            }

            log.info("[短信服务]发送验证码成功，手机号：{}", phoneNumber);

            // 设置验证码时效1分钟
            redisTemplate.opsForValue().set(key, "", 1, TimeUnit.MINUTES);

            return resp;
        } catch (ClientException e) {
            // 短信发送失败不做处理，用户如果没收到验证码可以选择重发一次
            log.info("[短信服务]发送验证码失败，手机号：{}", phoneNumber);
            return null;
        }
    }


}
