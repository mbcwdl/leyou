package com.leyou.order.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author guanlibin
 * @version 1.0
 * @create 2020/9/27 12:59
 */
@Data
@ConfigurationProperties("ly.worker")
public class IdWorkerProperties {
    private Long workerId;
    private Long datacenterId;
}
