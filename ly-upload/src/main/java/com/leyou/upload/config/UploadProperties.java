package com.leyou.upload.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * @author guanlibin
 * @version 1.0
 * @create 2020/9/19 9:17
 */
@ConfigurationProperties(prefix = "ly.upload")
@Data
public class UploadProperties {
    private String baseUrl;
    private List<String> allowTypes;
}
