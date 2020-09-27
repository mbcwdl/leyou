package com.leyou.order.config;

import com.github.wxpay.sdk.WXPay;
import com.github.wxpay.sdk.WXPayConstants;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author guanlibin
 * @version 1.0
 * @create 2020/9/27 16:31
 */
@Configuration
public class WXPayConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "ly.pay")
    public PayConfig payConfig() {
        return new PayConfig();
    }

    @Bean
    public WXPay wxPay(PayConfig config) {
        return new WXPay(config, WXPayConstants.SignType.HMACSHA256);
    }
}
