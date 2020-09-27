package com.leyou.gateway.config;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.w3c.dom.stylesheets.LinkStyle;

import java.util.List;

/**
 * @author guanlibin
 * @version 1.0
 * @create 2020/9/18 23:06
 */
@ConfigurationProperties(prefix = "ly.cros")
@Data
public class CrosAllowedOriginProperties {
    private List<String> allowedOrigin;
}
