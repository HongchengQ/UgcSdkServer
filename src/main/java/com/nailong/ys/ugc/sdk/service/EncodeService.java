package com.nailong.ys.ugc.sdk.service;

import com.nailong.ys.ugc.sdk.model.GiFileModel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static com.nailong.ys.ugc.sdk.constants.MagicNumberConstants.HEAD_MAGIC_NUMBER;
import static com.nailong.ys.ugc.sdk.constants.MagicNumberConstants.TAIL_MAGIC_NUMBER;

public class EncodeService {
//    public byte[] encodeGiFile(GiFileModel giFileModel) {
//        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//
//        try {
//            outputStream.write(intToByteArray(jsonData.length));
//            outputStream.write(intToByteArray(1)); // version
//            outputStream.write(HEAD_MAGIC_NUMBER);
//
//            // 写入数据
//            outputStream.write(jsonData);
//
//            outputStream.write(TAIL_MAGIC_NUMBER);
//
//        } catch (IOException e) {
//            log.error("生成二进制数据时出错", e);
//        }
//
//        return outputStream.toByteArray();
//    }


    /**
     * 整数转字节数组（大端序）
     */
    private byte[] intToByteArray(int value) {
        return new byte[]{
                (byte) (value >> 24),
                (byte) (value >> 16),
                (byte) (value >> 8),
                (byte) value
        };
    }
}
