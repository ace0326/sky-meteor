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
package com.sky.meteor.rpc.consumer.proxy.bytebuddy;

import com.sky.meteor.rpc.annotation.Reference;
import com.sky.meteor.rpc.consumer.proxy.AbstractProxyFactory;
import com.sky.meteor.common.spi.SpiMetadata;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * @author
 */
@Slf4j
@SpiMetadata(name = "byteBuddy")
public class ByteBuddyProxyFactory extends AbstractProxyFactory {

    @Override
    public <T> T newInstance(Class<?> interfaceClass, Reference reference) {
        ByteBuddyProxy byteBuddyProxy = new ByteBuddyProxy(interfaceClass, reference);
        Class<?> cls = new ByteBuddy()
                .subclass(interfaceClass)
                .method(ElementMatchers.isDeclaredBy(interfaceClass))
                .intercept(MethodDelegation.to(byteBuddyProxy, "handler"))
                .make()
                .load(interfaceClass.getClassLoader(), ClassLoadingStrategy.Default.INJECTION)
                .getLoaded();
        return newInstance(interfaceClass, cls);
    }

    public <T> T newInstance(Class<?> interfaceClass, Class<?> cls) {
        try {
            return (T) cls.newInstance();
        } catch (Exception e) {
            log.error("rpc spring consumer generate bytebuddy proxy instance fail:{}" + interfaceClass.getName(), e);
        }
        return null;
    }

}
