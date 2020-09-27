package com.leyou.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * @author guanlibin
 * @version 1.0
 * @create 2020/9/26 13:42
 */
@Data
@ConfigurationProperties("ly.filter")
public class FilterProperties {

    private List<String> allowPaths;
}
