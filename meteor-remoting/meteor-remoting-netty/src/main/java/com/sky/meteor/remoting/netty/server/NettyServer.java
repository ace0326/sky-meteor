/*
 * The MIT License (MIT)
 * Copyright © 2019-2020 <sky>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the “Software”), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.sky.meteor.remoting.netty.server;


import com.sky.meteor.common.spi.SpiExtensionHolder;
import com.sky.meteor.common.threadpool.ThreadPoolHelper;
import com.sky.meteor.registry.Register;
import com.sky.meteor.registry.Registry;
import com.sky.meteor.registry.RegistryService;
import com.sky.meteor.remoting.AbstractBootstrap;
import com.sky.meteor.remoting.netty.InternalHandler;
import com.sky.meteor.remoting.netty.Processor;
import com.sky.meteor.remoting.netty.protocol.ProtocolDecoder;
import com.sky.meteor.remoting.netty.protocol.ProtocolEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.flush.FlushConsolidationHandler;
import io.netty.util.Version;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author
 */
@Slf4j
public class NettyServer extends AbstractBootstrap implements Registry, InternalHandler {
    /**
     * 启动项
     */
    private ServerBootstrap bootstrap = null;
    /**
     * boss 线程组
     */
    private EventLoopGroup bossGroup = null;
    /**
     * worker 线程组
     */
    private EventLoopGroup workerGroup = null;
    /**
     *
     */
    private Channel channel = null;
    /**
     * 服务端默认端口
     */
    private static final int DEFAULT_PORT = 8080;
    /**
     * 注册中心
     */
    @Getter
    private RegistryService registryService = null;
    /**
     * 端口号
     */
    private int port;
    /**
     * 内部处理器
     */
    private Processor processor;

    public NettyServer() {
        this(DEFAULT_PORT);
    }

    public NettyServer(int port) {
        this.port = port;
    }


    @Override
    public void start() {
        super.start();
        try {
            this.init();
            ChannelFuture channelFuture = bootstrap.bind(port).sync();
            channel = channelFuture.channel();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> stop()));
            log.info("the server start successfully ! {}", Version.identify().entrySet());
            channel.closeFuture().sync();
        } catch (Exception e) {
            log.error("the server start failed:{}", e.getMessage());
            stop();
        }
    }


    @Override
    public void stop() {
        if (status()) {
            super.stop();
            try {
                ThreadPoolHelper.shutdown();
            } catch (Throwable e) {
                log.warn(e.getMessage(), e);
            }
            try {
                if (channel != null) {
                    channel.close();
                }
            } catch (Throwable e) {
                log.warn(e.getMessage(), e);
            }
            try {
                if (bootstrap != null) {
                    bossGroup.shutdownGracefully();
                    workerGroup.shutdownGracefully();
                }
            } catch (Throwable e) {
                log.warn(e.getMessage(), e);
            }
        } else {
            log.info(" the server has been shutdown !");
        }
    }

    /**
     *
     */
    @Override
    public void init() {
        //epoll kqueue
        bossGroup = new NioEventLoopGroup(0, new DefaultThreadFactory("boss", true));
        workerGroup = new NioEventLoopGroup(0, new DefaultThreadFactory("worker", true));
        ServerChannelHandler serverChannelHandler = new ServerChannelHandler(processor);
        MetricsChannelHandler metricsChannelHandler = new MetricsChannelHandler();
        /*LoggingHandler loggingHandler = new LoggingHandler(LogLevel.INFO);*/
        try {
            bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .option(ChannelOption.SO_REUSEADDR, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
                            p.addLast("metrics", metricsChannelHandler);
                            p.addLast(new ServerIdleStateTrigger());
                            p.addLast(new ServerHeartbeatChannelHandler());
                            p.addLast("protocolEncoder", new ProtocolEncoder());
                            p.addLast("protocolDecoder", new ProtocolDecoder());
                            p.addLast("flushEnhance", new FlushConsolidationHandler(5, true));
                            p.addLast("serverChannelHandler", serverChannelHandler);
                        }
                    })
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.SO_REUSEADDR, true)
                    .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        } catch (Exception e) {
            log.error("the server init failed:{}", e.getMessage());
        }
    }

    @Override
    public void connectToRegistryServer(Register register) {
        SpiExtensionHolder.getInstance().loadSpiExtension(RegistryService.class, register.getName());
        registryService = SpiExtensionHolder.getInstance().get(RegistryService.class);
        registryService.connectToRegistryServer(register);
    }

    @Override
    public void setProcessor(Processor processor) {
        this.processor = processor;
    }
}
