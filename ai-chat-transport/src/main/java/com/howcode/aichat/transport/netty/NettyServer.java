package com.howcode.aichat.transport.netty;

import com.howcode.aichat.transport.dispatch.ChatWebSocketHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/** Netty WebSocket 服务端：装配 pipeline 并绑定端口。 */
@Component
public class NettyServer implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(NettyServer.class);

    private final NettyProperties properties;
    private final ChatWebSocketHandler chatHandler;
    private final IdleDetectHandler idleHandler;
    private NioEventLoopGroup bossGroup;
    private NioEventLoopGroup workerGroup;

    public NettyServer(NettyProperties properties,
                       ChatWebSocketHandler chatHandler,
                       IdleDetectHandler idleHandler) {
        this.properties = properties;
        this.chatHandler = chatHandler;
        this.idleHandler = idleHandler;
    }

    @Override
    public void afterPropertiesSet() {
        Thread thread = new Thread(this::start, "aichat-netty-server");
        thread.start();
    }

    private void start() {
        bossGroup = new NioEventLoopGroup(properties.getBossThreads());
        workerGroup = new NioEventLoopGroup(properties.getWorkerThreads());
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel channel) {
                        channel.pipeline().addLast(new HttpServerCodec());
                        channel.pipeline().addLast(new ChunkedWriteHandler());
                        channel.pipeline().addLast(new HttpObjectAggregator(65535));
                        channel.pipeline().addLast(new WebSocketServerProtocolHandler("/"));
                        channel.pipeline().addLast(new IdleStateHandler(0, 0,
                                properties.getIdleMinutes(), TimeUnit.MINUTES));
                        channel.pipeline().addLast(idleHandler);
                        channel.pipeline().addLast(chatHandler);
                    }
                });
        try {
            ChannelFuture future = bootstrap.bind(properties.getPort()).sync();
            LOGGER.info("Netty 服务启动成功，端口 {}", properties.getPort());
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            LOGGER.error("Netty 启动失败", e);
        }
    }

    @PreDestroy
    public void stop() {
        if (null != bossGroup) {
            bossGroup.shutdownGracefully();
        }
        if (null != workerGroup) {
            workerGroup.shutdownGracefully();
        }
    }
}
