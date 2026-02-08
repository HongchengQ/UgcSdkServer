package com.nailong.ys.ugc.sdk.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app")
@Data
public class AppConfiguration {
    int gameFileVersion;
    int headMagicNumberHex;
    int tailMagicNumberHex;

    // test
//    @PostConstruct
//    public void init() {
//        System.out.println(Arrays.toString(intToByteArray(0x326)) + "|||" + Arrays.toString(intToByteArray(headMagicNumberHex)));
//    }
}
