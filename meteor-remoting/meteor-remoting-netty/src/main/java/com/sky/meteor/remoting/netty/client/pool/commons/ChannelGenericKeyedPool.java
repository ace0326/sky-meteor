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
//package com.sky.meteor.remoting.netty.client.pool;
//
//import com.sky.meteor.remoting.netty.client.NettyClient;
//import io.netty.channel.Channel;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.pool.impl.GenericKeyedObjectPool;
//
///**
// * @author
// */
//@Slf4j
//public class ChannelGenericKeyedPool {
//
//    private GenericKeyedObjectPool pool;
//
//    public ChannelGenericKeyedPool(NettyClient client) {
//        GenericKeyedObjectPool.Config config = new GenericKeyedObjectPool.Config();
//        config.maxActive = 20;
//        config.maxWait = 3000;
//        config.minIdle = 10;
//        config.testWhileIdle = true;
//        config.numTestsPerEvictionRun = 1;
//        config.timeBetweenEvictionRunsMillis = 1000;
//        pool = new GenericKeyedObjectPool(new KeyConnectionFactory(client), config);
//    }
//
//    public Channel getConnection(String key) throws Exception {
//        return (Channel) pool.borrowObject(key);
//    }
//
//    public void releaseConnection(String key, Channel channel) {
//        try {
//            pool.returnObject(key, channel);
//        } catch (Exception e) {
//            e.printStackTrace();
//            if (channel != null) {
//                try {
//                    channel.close();
//                } catch (Exception ex) {
//                    ex.printStackTrace();
//                }
//            }
//        }
//    }
//}
