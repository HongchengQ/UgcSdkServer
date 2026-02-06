package com.nailong.ys.ugc.sdk.constants;

import org.apache.tomcat.util.buf.HexUtils;

public class MagicNumberConstants {
    public static final byte[] HEAD_MAGIC_NUMBER = HexUtils.fromHexString("00000326"); // 头部魔数
    public static final byte[] TAIL_MAGIC_NUMBER = HexUtils.fromHexString("00000679"); // 尾部魔数
}
