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
package com.sky.meteor.spring.support;

import com.sky.meteor.registry.Register;
import com.sky.meteor.registry.zookeeper.ZookeeperRegister;
import com.sky.meteor.remoting.netty.Processor;
import com.sky.meteor.remoting.netty.client.NettyClient;
import com.sky.meteor.rpc.consumer.ConsumerProcessorHandler;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.InitializingBean;

import java.util.List;

/**
 * @author
 */
public class MeteorSpringClient implements InitializingBean {
    @Getter
    @Setter
    private String appName;
    @Getter
    @Setter
    private String registryType;
    @Getter
    @Setter
    private String registryServerAddresses;
    @Getter
    @Setter
    private String group;
    @Getter
    @Setter
    private List<Pair<String, Object>> options;
    @Getter
    @Setter
    private String serializerType;
    @Getter
    @Setter
    private String loadBalancerType;
    @Getter
    private NettyClient nettyClient;

    @Override
    public void afterPropertiesSet() throws Exception {
        init();
    }

    private void init() {
        Register register = new ZookeeperRegister();
        register.setConnect(registryServerAddresses);
        register.setGroup(group);
        register.setName(registryType);
        Processor processor = new ConsumerProcessorHandler();
        nettyClient = new NettyClient();
        nettyClient.connectToRegistryServer(register);
        nettyClient.setProcessor(processor);
        nettyClient.startup();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> nettyClient.shutdown()));
    }

}
