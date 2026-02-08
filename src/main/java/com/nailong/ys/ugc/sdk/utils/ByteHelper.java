package com.nailong.ys.ugc.sdk.utils;

import java.util.Arrays;

import static com.nailong.ys.ugc.sdk.utils.ByteUtils.bytesToInt;

public final class ByteHelper {

    /**
     *
     * @param rawData
     * @return 数组前4字节转int
     */
    public static int get4BytesToIntFromStart(byte[] rawData) {
        return bytesToInt(Arrays.copyOf(rawData, Math.min(4, rawData.length)));
    }
}
