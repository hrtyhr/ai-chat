package com.howcode.aichat.transport.netty;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** Netty 配置。 */
@Data
@Component
@ConfigurationProperties(prefix = "aichat.netty")
public class NettyProperties {
    private int port = 9090;
    private int bossThreads = 1;
    private int workerThreads = 8;
    private int idleMinutes = 10;
}
