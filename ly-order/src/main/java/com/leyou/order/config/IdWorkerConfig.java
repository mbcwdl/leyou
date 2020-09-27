package com.leyou.order.config;

import com.leyou.common.utils.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author guanlibin
 * @version 1.0
 * @create 2020/9/27 13:00
 */
@Configuration
@EnableConfigurationProperties({IdWorkerProperties.class})
public class IdWorkerConfig {

    @Bean
    public IdWorker idWorker(IdWorkerProperties properties) {
        return new IdWorker(properties.getWorkerId(), properties.getDatacenterId());
    }
}
