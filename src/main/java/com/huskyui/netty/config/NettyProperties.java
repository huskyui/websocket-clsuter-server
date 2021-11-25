package com.huskyui.netty.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author 王鹏
 */
@Data
@ConfigurationProperties(prefix = "netty")
public class NettyProperties {
    private int websocketPort;
    private String websocketPath;
    private int bossCount;
    private int workerCount;
}
