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
        for (File file : findFilesByExtension("./input", new String[]{".gil", ".gia"})) {
            String fileName = file.getName();

            GiFileModel fileModel = decodeGiFile(String.valueOf(file));

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

    public GiFileModel decodeGiFile(String path) throws IOException {
        GiFileModel giFileModel = new GiFileModel();

        byte[] rawData = fileConvertToByteArray(new File(path));

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

        /* fileSize */
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
                case GIL -> proto = decodeGilFile(rawData);
                case GIA -> proto = decodeGiaFile(rawData);
                default -> log.warn("Proto 解析时规则未命中，跳过");
            }

            if (proto != null) {
                giFileModel.setProtoMessage(proto);
            }
        }

        log.info("已成功解析:{}", giFileModel);
        return giFileModel;
    }

    public UgcGilArchiveInfoBin decodeGilFile(byte[] data) throws IOException {
        return UgcGilArchiveInfoBin.parseFrom(data);
    }

    public UgcGiaArchiveInfoBin decodeGiaFile(byte[] data) throws IOException {
        return UgcGiaArchiveInfoBin.parseFrom(data);
    }

    /**
     * 检查文件中定义的后续数据长度与实际是否相等
     *
     * @return 如果长度相等 则将长度定义删除并返回剩余内容
     */
    private void checkDataLength(int checkDataSize, int nextDataSize) throws RuntimeException {

        if (checkDataSize != nextDataSize) {
            log.error("data长度验证不正确,后续真实数据长度:{}, 预期长度应该是:{}", nextDataSize, checkDataSize);
            throw new RuntimeException();
        }

        log.info("已成功验证data长度");
    }

    private void checkMagicNumber(boolean isHead, byte[] rawData) throws RuntimeException {
        byte[] fileMagicNumber;

        if (isHead) {
            fileMagicNumber = Arrays.copyOf(rawData, Math.min(HEAD_MAGIC_NUMBER.length, rawData.length));
            if (Arrays.equals(fileMagicNumber, HEAD_MAGIC_NUMBER)) {
                return;
            }
        } else {
            fileMagicNumber = getTailBytes(rawData, TAIL_MAGIC_NUMBER.length);
            if (Arrays.equals(fileMagicNumber, TAIL_MAGIC_NUMBER)) {
                return;
            }
        }

        log.error("魔数校验失败,当前是否为头部:{}", isHead);
        throw new RuntimeException();
    }
}