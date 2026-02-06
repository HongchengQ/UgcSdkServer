package com.nailong.ys.ugc.sdk;

import com.nailong.ys.ugc.sdk.service.DecodeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestComponent;

import java.io.IOException;

@SpringBootTest
@TestComponent

class UgcSdkServerApplicationTests {

    @Test
    void contextLoads() {
    }

    @Autowired
    DecodeService decodeService;

    @Test
    void setDecodeService() throws IOException {
        decodeService.decodeGilProto("./input/官方教程-导出.gil");
    }
}
