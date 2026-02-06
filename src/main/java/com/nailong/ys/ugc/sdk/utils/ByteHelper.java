package com.nailong.ys.ugc.sdk.utils;

import java.util.Arrays;

import static com.nailong.ys.ugc.sdk.utils.ByteUtils.byteArrayToIntManually;

public final class ByteHelper {

    /**
     *
     * @param rawData
     * @return 数组前4字节转int
     */
    public static int get4BytesToIntFromStart(byte[] rawData) {
        return byteArrayToIntManually(Arrays.copyOf(rawData, Math.min(4, rawData.length)));
    }
}
