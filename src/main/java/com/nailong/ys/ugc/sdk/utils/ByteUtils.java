package com.nailong.ys.ugc.sdk.utils;

import lombok.extern.log4j.Log4j2;

import java.nio.ByteBuffer;
import java.util.Arrays;

@Log4j2
public final class ByteUtils {
    /**
     * byteBuffer 转 byte数组
     *
     * @param buffer
     * @return
     */
    public static byte[] bytebufferToByteArray(ByteBuffer buffer) {
        //重置 limit 和postion 值
        buffer.flip();
        //获取buffer中有效大小
        int len = buffer.limit() - buffer.position();

        byte[] bytes = new byte[len];

        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = buffer.get();

        }

        return bytes;
    }

    public static int bytesToInt(byte[] bytes) {
        int intValue = 0;
        for (int i = 0; i < 4; i++) {
            intValue = (intValue << 8) | (bytes[i] & 0xFF);
        }
        return intValue;
    }

    /**
     * 整数转字节数组（大端序）
     */
    public static byte[] intToByteArray(int value) {
        return new byte[]{
                (byte) (value >> 24),
                (byte) (value >> 16),
                (byte) (value >> 8),
                (byte) value
        };
    }

    /**
     * 从字节数组开头移除指定数量的字节
     *
     * @param source 源字节数组
     * @param count  要移除的字节数
     * @return 移除后的字节数组
     */
    public static byte[] removeBytesFromStart(byte[] source, int count) {
        if (source == null || count <= 0) {
            return source;
        }
        if (count >= source.length) {
            return new byte[0];
        }
        return Arrays.copyOfRange(source, count, source.length);
    }

    /**
     * 从字节数组尾部移除指定数量的字节
     *
     * @param source 源字节数组
     * @param count  要移除的字节数
     * @return 移除后的字节数组
     */
    public static byte[] removeBytesFromEnd(byte[] source, int count) {
        if (source == null || count <= 0) {
            return source;
        }
        if (count >= source.length) {
            return new byte[0];
        }
        return Arrays.copyOf(source, source.length - count);
    }

    /**
     * 只保留字节数组中间指定范围的数据
     *
     * @param source     源字节数组
     * @param startIndex 开始索引（包含）
     * @param endIndex   结束索引（不包含）
     * @return 指定范围的字节数组
     */
    public static byte[] extractMiddleRange(byte[] source, int startIndex, int endIndex) {
        if (source == null || startIndex < 0 || endIndex > source.length || startIndex >= endIndex) {
            return new byte[0];
        }
        return Arrays.copyOfRange(source, startIndex, endIndex);
    }

    /**
     * 获取字节数组尾部指定数量的字节
     *
     * @param source 源字节数组
     * @param count  要获取的字节数
     * @return 尾部的字节数组
     */
    public static byte[] getTailBytes(byte[] source, int count) {
        if (source == null || count <= 0) {
            return new byte[0];
        }
        if (count >= source.length) {
            return source.clone(); // 返回完整数组的副本
        }
        return Arrays.copyOfRange(source, source.length - count, source.length);
    }
}
