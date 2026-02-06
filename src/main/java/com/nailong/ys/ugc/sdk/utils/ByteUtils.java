package com.nailong.ys.ugc.sdk.utils;

import lombok.extern.log4j.Log4j2;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

@Log4j2
public final class ByteUtils {

    public static int byteArrayToIntManually(byte[] bytes) {
        int intValue = 0;
        for (int i = 0; i < 4; i++) {
            intValue = (intValue << 8) | (bytes[i] & 0xFF);
        }
        return intValue;
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
