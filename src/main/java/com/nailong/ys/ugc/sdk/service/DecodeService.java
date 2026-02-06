package com.nailong.ys.ugc.sdk.service;

import com.google.protobuf.util.JsonFormat;
import com.nailong.ys.ugc.proto.UgcGilArchiveInfoBin;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.apache.tomcat.util.buf.HexUtils;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static com.nailong.ys.ugc.sdk.utils.ByteUtils.*;
import static com.nailong.ys.ugc.sdk.utils.FileUtils.fileConvertToByteArray;
import static com.nailong.ys.ugc.sdk.utils.FileUtils.findFilesByExtension;

@Service
@Log4j2
public class DecodeService {
    // 魔数
    byte[] headMagicNumber = HexUtils.fromHexString("000000010000032600000002"); // 头部魔数
    byte[] tailMagicNumber = HexUtils.fromHexString("00000679");                 // 尾部魔数

    /**
     * 启动时解包所有本地存档
     */
    @PostConstruct
    public void init() throws IOException {
        for (File file : findFilesByExtension("./input", new String[]{".gil"})) {
            String fileName = file.getName();

            UgcGilArchiveInfoBin ugcGilArchiveInfoBin = decodeGilProto(String.valueOf(file));

            try (BufferedWriter writer = Files.newBufferedWriter(Path.of("./output/" + fileName.replace(".gil", ".jsonc")))) {
                writer.write(JsonFormat.printer().print(ugcGilArchiveInfoBin));
                log.info("已生成{}", "./output/" + fileName.replace(".gil", ".jsonc"));
            }
        }
    }

    /**
     * 智能解码UGC数据 - 自动处理多层编码
     *
     * @param path 文件路径
     */
    public UgcGilArchiveInfoBin decodeGilProto(String path) throws IOException {
        // data长度+data
        byte[] rawData = fileConvertToByteArray(new File(path));

        // data: 魔数头+proto+魔数尾
        byte[] protoData = checkDataLength(rawData);

        // 头部魔数
        if (!Arrays.equals(Arrays.copyOf(protoData, Math.min(headMagicNumber.length, protoData.length)), headMagicNumber)) {
            log.error("头部魔数验证未通过");
            return null;
        } else {
            // 更新data 删掉头部魔数
            protoData = removeBytesFromStart(protoData, headMagicNumber.length);
            log.info("头部魔数验证通过,移除头部魔数, data_size:{}", protoData.length);
        }

        // 尾部魔数
        if (!Arrays.equals(getTailBytes(protoData, tailMagicNumber.length), tailMagicNumber)) {
            log.error("尾部魔数验证未通过");
            return null;
        } else {
            // 更新data 删掉头部魔数
            protoData = removeBytesFromEnd(protoData, tailMagicNumber.length);
            log.info("尾部魔数验证通过,移除尾部魔数, data_size:{}", protoData.length);
        }

        protoData = checkDataLength(protoData);

        return UgcGilArchiveInfoBin.parseFrom(protoData);
    }

    /**
     * 检查文件中定义的后续数据长度与实际是否相等
     *
     * @return 如果长度相等 则将长度定义删除并返回剩余内容
     */
    private byte[] checkDataLength(byte[] rawData) {
        byte[] protoData = removeBytesFromStart(rawData, 4);

        int defineTheLength = byteArrayToIntManually(Arrays.copyOf(rawData, Math.min(4, rawData.length)));

        if (defineTheLength != protoData.length) {
            log.error("data长度验证不正确, 原文件大小:{}, 后续真实数据长度:{}, 长度应该是:{}", rawData.length, protoData.length, defineTheLength);
            throw new RuntimeException();
        }

        log.info("已成功验证data长度, 原文件大小:{}, 后续真实数据长度:{}, 长度应该是:{}", rawData.length, protoData.length, defineTheLength);
        return protoData;
    }
}