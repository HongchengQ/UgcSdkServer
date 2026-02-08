package com.nailong.ys.ugc.sdk.dto;

import lombok.Data;

/**
 * 文件转换请求DTO - 使用base64传输二进制数据
 */
@Data
public class ConversionData {
    private String fileName;    // 原始文件名
    private String fileType;    // 原始文件类型
    private String base64Data;  // 使用base64编码的文件数据
}