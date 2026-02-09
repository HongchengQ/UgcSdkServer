package com.nailong.ys.ugc.sdk.model;

import com.google.protobuf.GeneratedMessage;
import com.nailong.ys.ugc.sdk.enums.GiFileType;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class GiFileModel {
    ///
    /// 文件中应该存在的字段
    ///

    // 版本
    private int version;

    // 头魔数
    private int headMagicNumber;

    // 文件类型
    private GiFileType giFileType;

    // 内容长度
    private int dataLength;

    // 内容
    private GeneratedMessage protoMessage;

    // 尾魔数
    private int tailMagicNumber;

    ///
    /// 自定义附加字段，不存在于文件
    ///

    /* 所有元素需要的字节空间 */
    ElementTakesUpSpaceModel elementTakesUpSpace = new ElementTakesUpSpaceModel();


    public void setDataLength(int dataLength) {
        this.dataLength = dataLength;
        elementTakesUpSpace.setProtoMessageTakesUpSpace(dataLength);
    }

    @Override
    public String toString() {
        return "GiFileModel{" +
                "Version=" + version +
                ", HeadMagicNumber=" + headMagicNumber +
                ", GiFileType=" + giFileType +
                ", DataLength=" + dataLength +
                ", TailMagicNumber=" + tailMagicNumber +
                ", ProtoMessageSize=" + protoMessage.toByteArray().length +
                '}';
    }

    @Getter
    public static class ElementTakesUpSpaceModel {
        // 版本需要的空间
        private static final int versionTakesUpSpace = 4;

        // 头魔数需要的空间
        private static final int headMagicNumberTakesUpSpace = 4;

        // 文件类型需要的空间
        private static final int giFileTypeTakesUpSpace = 4;

        // 内容长度需要的空间
        private static final int dataLengthTakesUpSpace = 4;

        // 尾魔数需要的空间
        private static final int tailMagicNumberTakesUpSpace = 4;

        // 内容需要的空间
        @Setter
        private int protoMessageTakesUpSpace;


        /**
         * 所有基本类型
         *
         * @return 需要的字节空间
         */
        public static int getBasicElementTakesUpSpace() {
            return versionTakesUpSpace + headMagicNumberTakesUpSpace + giFileTypeTakesUpSpace +
                    dataLengthTakesUpSpace + tailMagicNumberTakesUpSpace;
        }

        /**
         * 所有类型 (基本+proto)
         *
         * @return 需要的字节空间
         */
        public int getAllElementTakesUpSpace() {
            return getBasicElementTakesUpSpace() + protoMessageTakesUpSpace;
        }
    }
}
