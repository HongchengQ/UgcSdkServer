package com.nailong.ys.ugc.sdk.model;

import com.google.protobuf.Message;
import com.nailong.ys.ugc.sdk.enums.GiFileType;
import lombok.Data;

@Data
public class  GiFileModel {
    // 文件大小
    private int fileSize;

    // 版本
    private int version;

    // 头魔数
    private int headMagicNumber;

    // 文件类型
    private GiFileType giFileType;

    // 内容长度
    private int dataLength;

    // 内容
    private Message protoMessage;

    // 尾魔数
    private int tailMagicNumber;

    @Override
    public String toString() {
        return "GiFileModel{" +
                "FileSize=" + fileSize +
                ", Version=" + version +
                ", HeadMagicNumber=" + headMagicNumber +
                ", GiFileType=" + giFileType +
                ", DataLength=" + dataLength +
                ", TailMagicNumber=" + tailMagicNumber +
                ", ProtoMessageSize=" + protoMessage.toByteArray().length +
                '}';
    }
}
