package com.nailong.ys.ugc.sdk.controller;

import com.nailong.ys.ugc.sdk.dto.ConversionData;
import com.nailong.ys.ugc.sdk.enums.GiFileType;
import com.nailong.ys.ugc.sdk.service.ConversionService;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

/**
 * 文件转换API控制器
 * 直接返回转换后的数据，简化响应结构
 */
@RestController
@RequestMapping("/api/conversion")
@CrossOrigin(origins = "*") // 允许跨域请求
public class ConversionController {

    @Resource
    private ConversionService conversionService;

    /**
     * 正向转换接口：二进制文件 → 指定格式
     * @param outputFormat 输出格式 (json1/json2/pb)
     * @param requestData 文件数据
     * @return 转换后的数据（直接返回）
     */
    @PostMapping("/forward")
    public ResponseEntity<?> forwardConvert(
            @RequestParam String outputFormat,
            @RequestBody ConversionData requestData) {
        
        try {
            // 验证必要参数
            if (requestData.getFileName() == null || requestData.getFileType() == null ||
                requestData.getBase64Data() == null) {
                return ResponseEntity.badRequest().body("缺少必要的请求参数");
            }

            // 执行正向转换，直接返回数据
            Object result = conversionService.processForwardConversion(outputFormat, requestData);
            
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("转换过程中发生错误: " + e.getMessage());
        }
    }

    /**
     * 反向转换接口：指定格式 → 二进制文件
     * @param targetFileType 目标文件类型 (gil/gia/gip/gir)
     * @param requestData 文件数据
     * @return base64编码的二进制数据
     */
    @PostMapping("/reverse")
    public ResponseEntity<?> reverseConvert(
            @RequestParam String targetFileType,
            @RequestBody ConversionData requestData) {
        
        try {
            // 验证必要参数
            if (requestData.getFileName() == null || requestData.getFileType() == null ||
                requestData.getBase64Data() == null) {
                return ResponseEntity.badRequest().body("缺少必要的请求参数");
            }

            GiFileType targetFileTypeEnum = GiFileType.NONE;

            if (ObjectUtils.isEmpty(targetFileType)){
                return ResponseEntity.status(502).build();
            }
            targetFileType = targetFileType.toLowerCase(Locale.ROOT);

            switch (targetFileType) {
                case "gil" -> targetFileTypeEnum = GiFileType.GIL;
                case "gia" -> targetFileTypeEnum = GiFileType.GIA;
                case "gip" -> targetFileTypeEnum = GiFileType.GIP;
                case "gir" -> targetFileTypeEnum = GiFileType.GIR;
            }

            // 执行反向转换，返回base64编码的数据
            String result = conversionService.processReverseConversion(targetFileTypeEnum, requestData);
            
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("转换过程中发生错误: " + e.getMessage());
        }
    }
}