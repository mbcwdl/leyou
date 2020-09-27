package com.leyou.order.config;

import com.leyou.auth.utils.RsaUtils;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.security.PublicKey;

/**
 * @author guanlibin
 * @version 1.0
 * @create 2020/9/26 18:00
 */
@Data
@ConfigurationProperties(prefix = "ly.jwt")
public class JwtProperties {

    private String pubKeyPath;// 公钥
    private PublicKey publicKey;
    private String cookieName;

    @PostConstruct
    public void init() throws Exception {
        // 读取公钥和私钥
        publicKey = RsaUtils.getPublicKey(pubKeyPath);
    }
}
