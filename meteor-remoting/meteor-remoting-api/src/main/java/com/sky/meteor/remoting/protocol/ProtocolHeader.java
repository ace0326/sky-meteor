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
package com.sky.meteor.remoting.protocol;

import lombok.Getter;
import lombok.Setter;

/**
 * 传输层协议头
 * <p>
 * **************************************************************************************************
 * Protocol
 * ┌ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┐
 * 2   │   1   │    1   │     8     │      4      │
 * ├ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┤
 * │       │        │           │             │
 * │  MAGIC   Sign    Status   Invoke Id    Body Size                    Body Content              │
 * │       │        │           │             │
 * └ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┘
 * <p>
 * 消息头16个字节定长
 * = 2 // magic = (short) 0xbabe
 * + 1 // 消息标志位, 低地址4位用来表示消息类型request/response/heartbeat等, 高地址4位用来表示序列化类型
 * + 1 // 状态位, 设置请求响应状态
 * + 8 // 消息 id, long 类型, 未来可能将id限制在48位, 留出高地址的16位作为扩展字段
 * + 4 // 消息体 body 长度, int 类型
 *
 * @author
 */
public class ProtocolHeader {

    /**
     * 协议头长度
     */
    public static final int HEADER_SIZE = 16;
    /**
     * Magic
     */
    public static final short MAGIC = (short) 0xbabe;

    /**===================================================================================**/

    /**
     * Message Code: 0x01 ~ 0x0f
     */
    /**
     * Request
     */
    public static final byte REQUEST = 0x01;
    /**
     * Response
     */
    public static final byte RESPONSE = 0x02;
    /**
     * 发布服务
     */
    public static final byte PUBLISH_SERVICE = 0x03;
    /**
     * 取消发布服务
     */
    public static final byte PUBLISH_CANCEL_SERVICE = 0x04;
    /**
     * 订阅服务
     */
    public static final byte SUBSCRIBE_SERVICE = 0x05;
    /**
     * 通知下线
     */
    public static final byte OFFLINE_NOTICE = 0x06;
    /**
     * Acknowledge
     */
    public static final byte ACK = 0x07;
    /**
     * Heartbeat
     */
    public static final byte HEARTBEAT = 0x0f;
    /**
     * sign 低地址4位
     */
    @Getter
    @Setter
    private byte messageCode;

    /**================================================================================**/

    /**
     * Serializer Code: 0x01 ~ 0x0f
     * 位数限制最多支持15种不同的序列化/反序列化方式
     * protostuff   = 0x01
     * hessian      = 0x02
     * kryo         = 0x03
     * java         = 0x04
     * fastjson     = 0x05
     * ...
     * XX1          = 0x0e
     * XX2          = 0x0f
     */
    @Getter
    @Setter
    private byte serializerCode;

    /**
     * 响应状态码
     */
    @Getter
    @Setter
    private byte status;
    /**
     * request.invokeId, 用于映射 <id, request, response> 三元组
     */
    @Getter
    @Setter
    private long id;
    /**
     * 消息体长度
     */
    @Getter
    @Setter
    private int bodySize;

    /**
     * 转换标志位
     *
     * @param serializerCode
     * @param messageCode
     * @return
     */
    public static byte toSign(byte serializerCode, byte messageCode) {
        return (byte) ((serializerCode << 4) | (messageCode & 0x0f));
    }

    /**
     * 解析标志位
     *
     * @param sign
     */
    public void sign(byte sign) {
        // sign 低地址4位
        this.messageCode = (byte) (sign & 0x0f);
        // sign 高地址4位, 先转成无符号int再右移4位
        this.serializerCode = (byte) ((((int) sign) & 0xff) >> 4);
    }

    @Override
    public String toString() {
        return "ProtocolHeader{" +
                "messageCode=" + messageCode +
                ", serializerCode=" + serializerCode +
                ", status=" + status +
                ", id=" + id +
                ", bodySize=" + bodySize +
                '}';
    }
}
