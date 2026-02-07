package com.nailong.ys.ugc.sdk.service;

import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import com.nailong.ys.ugc.sdk.dto.ConversionData;
import com.nailong.ys.ugc.sdk.model.GiFileModel;
import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

import static com.nailong.ys.ugc.sdk.constants.MagicNumberConstants.HEAD_MAGIC_NUMBER;
import static com.nailong.ys.ugc.sdk.constants.MagicNumberConstants.TAIL_MAGIC_NUMBER;

/**
 * 文件转换服务
 * 直接返回转换后的数据，简化响应结构
 */
@Service
@Log4j2
public class ConversionService {

    @Resource
    DecodeService decodeService;

    /**
     * 正向转换：二进制文件 → 指定格式
     *
     * @param outputFormat 输出格式 (json1/json2/pb)
     * @param requestData  包含base64编码数据的请求
     * @return 转换后的数据（直接返回，不包装在Response对象中）
     */
    public Object processForwardConversion(String outputFormat, ConversionData requestData) {
        try {
            // 解码base64数据
            byte[] fileData = Base64.getDecoder().decode(requestData.getBase64Data());

            // 执行正向转换
            Object result = processForwardConversionLogic(fileData, requestData, outputFormat);

            log.info("正向转换完成: {} -> {}格式", requestData.getFileName(), outputFormat);
            return result;

        } catch (Exception e) {
            log.error("正向转换失败", e);
            throw new RuntimeException("转换失败: " + e.getMessage(), e);
        }
    }

    /**
     * 反向转换：指定格式 → 二进制文件
     *
     * @param targetFileType 目标文件类型
     * @param requestData    包含base64编码数据的请求
     * @return base64编码的二进制数据
     */
    public String processReverseConversion(String targetFileType, ConversionData requestData) {
        try {
            // 解码base64数据
            byte[] fileData = Base64.getDecoder().decode(requestData.getBase64Data());

            // 执行反向转换
            byte[] binaryData = processReverseConversionLogic(fileData, requestData, targetFileType);

            // 将二进制数据转换为Base64字符串
            String base64Data = Base64.getEncoder().encodeToString(binaryData);

            log.info("反向转换完成: {} -> {}", targetFileType, requestData.getFileName());
            return base64Data;

        } catch (Exception e) {
            log.error("反向转换失败", e);
            throw new RuntimeException("转换失败: " + e.getMessage(), e);
        }
    }

    /**
     * 正向转换核心逻辑
     */
    private Object processForwardConversionLogic(byte[] fileData, ConversionData requestData, String outputFormat) {
        try {
            // 解码
            GiFileModel fileModel = decodeService.decodeGiFile(fileData);

            // 根据输出格式转换
            return convertToSpecificFormat(fileModel, outputFormat);

        } catch (Exception e) {
            throw new RuntimeException("正向转换失败: " + e.getMessage(), e);
        }
    }

    /**
     * 反向转换核心逻辑
     */
    private byte[] processReverseConversionLogic(byte[] fileData, ConversionData requestData, String targetFileType) {
        try {
            // 根据用户选择的目标文件类型进行转换
            return simulateReverseConversion(fileData, targetFileType);
        } catch (Exception e) {
            throw new RuntimeException("反向转换失败: " + e.getMessage(), e);
        }
    }

    /**
     * 将GiFileModel转换为指定格式
     */
    private Object convertToSpecificFormat(GiFileModel fileModel, String format) throws IOException {
        Message protoMessage = fileModel.getProtoMessage();
        if (protoMessage == null) {
            throw new IOException("无法获取Proto消息");
        }

        return switch (format.toLowerCase()) {
            case "json1", "json2" ->
                    // 标准JSON格式（紧凑）
                    JsonFormat.printer().preservingProtoFieldNames().print(protoMessage);
            case "pb" ->
                    // Protocol Buffer二进制格式
                    Base64.getEncoder().encodeToString(protoMessage.toByteArray());
            default -> throw new IllegalArgumentException("不支持的输出格式: " + format);
        };
    }

    /**
     * 模拟反向转换（实际实现需要根据JSON重建二进制结构）
     */
    private byte[] simulateReverseConversion(byte[] jsonData, String fileType) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            outputStream.write(intToByteArray(jsonData.length));
            outputStream.write(intToByteArray(1)); // version
            outputStream.write(HEAD_MAGIC_NUMBER);

            // 写入数据
            outputStream.write(jsonData);

            outputStream.write(TAIL_MAGIC_NUMBER);

        } catch (IOException e) {
            log.error("生成二进制数据时出错", e);
        }

        return outputStream.toByteArray();
    }

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