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
package com.sky.meteor.rpc.future;

import com.sky.meteor.common.exception.RpcException;
import com.sky.meteor.remoting.Response;
import com.sky.meteor.remoting.Status;
import com.sky.meteor.serialization.SerializerHolder;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

/**
 * @author
 */
@Slf4j
public class DefaultInvokeFuture<V> extends CompletableFuture<V> implements InvokeFuture<V> {

    private static final ConcurrentMap<Long, DefaultInvokeFuture<?>> roundFutures = new ConcurrentHashMap<>();

    private static final long DEFAULT_TIMEOUT_NANOSECONDS = TimeUnit.MILLISECONDS.toNanos(30000);

    private final long invokeId;

    private final Class<V> returnType;

    private final long timeout;

    private Throwable cause;

    private final long startTime = System.nanoTime();

    public static <T> DefaultInvokeFuture<T> with(
            long invokeId, long timeoutMillis, Class<T> returnType) {

        return new DefaultInvokeFuture<>(invokeId, timeoutMillis, returnType);
    }

    private DefaultInvokeFuture(long invokeId, long timeoutMillis, Class<V> returnType) {
        this.invokeId = invokeId;
        this.timeout = timeoutMillis > 0 ? TimeUnit.MILLISECONDS.toNanos(timeoutMillis) : DEFAULT_TIMEOUT_NANOSECONDS;
        this.returnType = returnType;
        roundFutures.put(invokeId, this);
    }

    @Override
    public Class<V> returnType() {
        return returnType;
    }

    @Override
    public V getResult() throws Throwable {
        try {
            return get(timeout, TimeUnit.NANOSECONDS);
        } catch (TimeoutException e) {
            Response response = new Response(invokeId);
            response.setStatus(Status.CLIENT_TIMEOUT.getKey());
            DefaultInvokeFuture.fakeReceived(response);
        }
        return null;
    }

    /**
     * @param response
     */
    private void doReceived(Response response) {
        byte status = response.getStatus();
        if (status == Status.OK.getKey()) {
            byte[] bytes = response.getBytes();
            byte serializerCode = response.getSerializerCode();
            V v = SerializerHolder.getInstance().getSerializer(serializerCode).deSerialize(bytes, returnType);
            complete(v);
        } else {
            setException(status);
        }
    }

    /**
     * set complete exception
     *
     * @param statusKey
     */
    private void setException(byte statusKey) {
        Status status = Status.parse(statusKey);
        Throwable cause = new RpcException(status.getKey(), status.getValue());
        setCause(cause);
        completeExceptionally(cause);
    }

    /**
     * @param response
     */
    public static void received(Response response) {
        long invokeId = response.getId();
        DefaultInvokeFuture<?> future = roundFutures.remove(invokeId);
        future.doReceived(response);
    }

    /**
     * @param response
     */
    public static void fakeReceived(Response response) {
        long invokeId = response.getId();
        DefaultInvokeFuture<?> future = roundFutures.remove(invokeId);
        future.doReceived(response);
    }


    @Override
    public Throwable getCause() {
        return cause;
    }

    public void setCause(Throwable cause) {
        this.cause = cause;
    }
}
