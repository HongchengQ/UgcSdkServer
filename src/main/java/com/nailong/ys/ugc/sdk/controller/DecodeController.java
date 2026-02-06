package com.nailong.ys.ugc.sdk.controller;

import com.nailong.ys.ugc.sdk.service.DecodeService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/decode")
class DecodeController {

    @Resource
    DecodeService decodeService;

    @RequestMapping("reload")
    public String decodeReload() throws IOException {
        decodeService.init();
        return "本地解包已完成";
    }
}
