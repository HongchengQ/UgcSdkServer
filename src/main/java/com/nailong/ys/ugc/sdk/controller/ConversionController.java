package com.nailong.ys.ugc.sdk.controller;

import com.nailong.ys.ugc.sdk.dto.ConversionData;
import com.nailong.ys.ugc.sdk.service.ConversionService;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

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

            // 执行反向转换，返回base64编码的数据
            String result = conversionService.processReverseConversion(targetFileType, requestData);
            
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("转换过程中发生错误: " + e.getMessage());
        }
    }

    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "healthy");
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }
    
    /**
     * 支持的格式列表
     */
    @GetMapping("/formats")
    public ResponseEntity<Map<String, Object>> getSupportedFormats() {
        Map<String, Object> response = new HashMap<>();
        
        Map<String, String[]> forwardFormats = new HashMap<>();
        forwardFormats.put("input", new String[]{"gil", "gia", "gip", "gir"});
        forwardFormats.put("output", new String[]{"json1", "json2", "pb"});
        
        Map<String, String[]> reverseFormats = new HashMap<>();
        reverseFormats.put("input", new String[]{"json1", "json2", "pb"});
        reverseFormats.put("output", new String[]{"gil", "gia", "gip", "gir"});
        
        response.put("forward", forwardFormats);
        response.put("reverse", reverseFormats);
        
        return ResponseEntity.ok(response);
    }
}