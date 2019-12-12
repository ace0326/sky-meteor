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
package com.sky.meteor.remoting.netty.client.pool;

import com.sky.meteor.registry.meta.RegisterMeta;
import lombok.Getter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author
 */
public class ChannelGenericPoolFactory {

    @Getter
    private static ConcurrentHashMap<RegisterMeta.Address, ChannelGenericPool> pools = new ConcurrentHashMap<>();

    private static final ReentrantLock lock = new ReentrantLock();

    /**
     * 创建channel对象池
     *
     * @param address
     */
    public static void create(RegisterMeta.Address address) {
        try {
            lock.lock();
            ChannelGenericPool channelGenericPool = pools.get(address);
            if (channelGenericPool == null) {
                channelGenericPool = new ChannelGenericPool(address.getHost() + ":" + address.getPort());
                pools.putIfAbsent(address, channelGenericPool);
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * 销毁所有channel对象池
     */
    public static void destroy() {
        pools.values().forEach(pool -> pool.close());
    }
}
