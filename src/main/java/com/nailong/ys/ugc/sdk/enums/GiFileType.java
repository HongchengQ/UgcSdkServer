package com.nailong.ys.ugc.sdk.enums;

import lombok.Getter;

@Getter
public enum GiFileType {
    NONE(0x0),
    GIP(0x1),
    GIL(0x2),
    GIA(0x3),
    GIR(0x4);

    final int value;

    GiFileType(int i) {
        value = i;
    }
}
