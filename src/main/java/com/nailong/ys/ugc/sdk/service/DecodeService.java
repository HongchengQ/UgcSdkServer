package com.nailong.ys.ugc.sdk.service;

import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import com.nailong.ys.ugc.proto.gia.UgcGiaArchiveInfoBin;
import com.nailong.ys.ugc.proto.gil.UgcGilArchiveInfoBin;
import com.nailong.ys.ugc.sdk.enums.GiFileType;
import com.nailong.ys.ugc.sdk.model.GiFileModel;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static com.nailong.ys.ugc.sdk.constants.MagicNumberConstants.HEAD_MAGIC_NUMBER;
import static com.nailong.ys.ugc.sdk.constants.MagicNumberConstants.TAIL_MAGIC_NUMBER;
import static com.nailong.ys.ugc.sdk.utils.ByteHelper.get4BytesToIntFromStart;
import static com.nailong.ys.ugc.sdk.utils.ByteUtils.*;
import static com.nailong.ys.ugc.sdk.utils.FileUtils.fileConvertToByteArray;
import static com.nailong.ys.ugc.sdk.utils.FileUtils.findFilesByExtension;

@Service
@Log4j2
public class DecodeService {

    /**
     * 启动时解包所有本地存档
     */
    @PostConstruct
    public void init() throws IOException {
        for (File file : findFilesByExtension("./input", new String[]{".gil", ".gia", "gip", "gir"})) {
            String fileName = file.getName();

            GiFileModel fileModel = decodeGiFile(fileConvertToByteArray(new File(String.valueOf(file))));

            switch (fileModel.getGiFileType()) {
                case GIL -> {
                    UgcGilArchiveInfoBin ugcGilArchiveInfoBin = (UgcGilArchiveInfoBin) fileModel.getProtoMessage();

                    fileName = fileName.replace(".gil", ".jsonc");

                    try (BufferedWriter writer = Files.newBufferedWriter(Path.of("./output/" + fileName))) {
                        writer.write(JsonFormat.printer().print(ugcGilArchiveInfoBin));
                        log.info("已生成{}", "./output/" + fileName);
                    }
                }
                case GIA -> {
                    UgcGiaArchiveInfoBin ugcGilArchiveInfoBin = (UgcGiaArchiveInfoBin) fileModel.getProtoMessage();

                    fileName = fileName.replace(".gia", ".jsonc");

                    try (BufferedWriter writer = Files.newBufferedWriter(Path.of("./output/" + fileName))) {
                        writer.write(JsonFormat.printer().print(ugcGilArchiveInfoBin));
                        log.info("已生成{}", "./output/" + fileName);
                    }
                }
                case GIP, GIR -> log.warn("当前还未支持 {GIP, GIR} 格式");
                default -> log.warn("未解析任何内容，因为规则未命中");
            }
        }
    }

    public GiFileModel decodeGiFile(byte[] rawData) throws IOException {
        GiFileModel giFileModel = new GiFileModel();

        /* file size */
        {
            // 读取前四字节转int，用于与后续真实内容长度进行比较
            int declaredDataSize = get4BytesToIntFromStart(rawData);

            // 头部删除四字节
            rawData = removeBytesFromStart(rawData, 4);

            checkDataLength(declaredDataSize, rawData.length);

            giFileModel.setFileSize(declaredDataSize);
        }

        /* version */
        {
            // 读取前四字节转int
            int version = get4BytesToIntFromStart(rawData);

            // 头部删除四字节
            rawData = removeBytesFromStart(rawData, 4);

            giFileModel.setVersion(version);
        }

        /* 头部魔数 */
        {
            checkMagicNumber(true, rawData);

            // 删除头部魔数
            rawData = removeBytesFromStart(rawData, HEAD_MAGIC_NUMBER.length);

            giFileModel.setHeadMagicNumber(byteArrayToIntManually(HEAD_MAGIC_NUMBER));

            log.info("头部魔数验证通过,移除头部魔数, data_size:{}", rawData.length);
        }

        /* 尾部魔数 */
        {
            checkMagicNumber(false, rawData);

            // 删除尾部魔数
            rawData = removeBytesFromEnd(rawData, TAIL_MAGIC_NUMBER.length);

            giFileModel.setTailMagicNumber(byteArrayToIntManually(TAIL_MAGIC_NUMBER));

            log.info("尾部魔数验证通过,移除尾部魔数, data_size:{}", rawData.length);
        }

        /* 文件类型 */
        {
            // 读取前四字节转int
            int version = get4BytesToIntFromStart(rawData);

            // 头部删除四字节
            rawData = removeBytesFromStart(rawData, 4);

            // 转枚举
            GiFileType fileType = GiFileType.values()[version];

            giFileModel.setGiFileType(fileType);
        }

        /* data size */
        {
            // 读取前四字节转int，用于与后续真实内容长度进行比较
            int declaredDataSize = get4BytesToIntFromStart(rawData);

            // 头部删除四字节
            rawData = removeBytesFromStart(rawData, 4);

            checkDataLength(declaredDataSize, rawData.length);

            giFileModel.setDataLength(declaredDataSize);
        }

        /* proto message */
        {
            Message proto = null;

            switch (giFileModel.getGiFileType()) {
                case GIL -> proto = UgcGilArchiveInfoBin.parseFrom(rawData);
                case GIA -> proto = UgcGiaArchiveInfoBin.parseFrom(rawData);
                default -> log.warn("Proto 解析时规则未命中，跳过");
            }

            if (proto != null) {
                giFileModel.setProtoMessage(proto);
            }
        }

        log.info("已成功解析:{}", giFileModel);
        return giFileModel;
    }

    /**
     * 检查文件中定义的后续数据长度与实际是否相等
     */
    private void checkDataLength(int checkDataSize, int nextDataSize) throws RuntimeException {
        if (checkDataSize != nextDataSize) {
            throw new RuntimeException(String.format(
                    "数据长度验证失败: 实际长度 %d, 预期长度 %d",
                    nextDataSize, checkDataSize
            ));
        }
        log.info("已成功验证data长度");
    }

    /**
     * 检查魔数
     * @param isHead 是头还是尾
     * @param rawData 原始数据
     * @throws RuntimeException 魔数校验失败
     */
    private void checkMagicNumber(boolean isHead, byte[] rawData) throws RuntimeException {
        byte[] fileMagicNumber;
        byte[] expectedMagicNumber = isHead ? HEAD_MAGIC_NUMBER : TAIL_MAGIC_NUMBER;

        if (isHead) {
            fileMagicNumber = java.util.Arrays.copyOf(rawData, Math.min(expectedMagicNumber.length, rawData.length));
        } else {
            fileMagicNumber = getTailBytes(rawData, expectedMagicNumber.length);
        }

        if (!java.util.Arrays.equals(fileMagicNumber, expectedMagicNumber)) {
            throw new RuntimeException("魔数校验失败");
        }
    }
}