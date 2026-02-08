package com.nailong.ys.ugc.sdk.service;

import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import com.nailong.ys.ugc.proto.gia.UgcGiaArchiveInfoBin;
import com.nailong.ys.ugc.proto.gil.UgcGilArchiveInfoBin;
import com.nailong.ys.ugc.sdk.constants.MagicNumberConstants;
import com.nailong.ys.ugc.sdk.enums.GiFileType;
import com.nailong.ys.ugc.sdk.model.GiFileModel;
import com.nailong.ys.ugc.sdk.utils.ByteUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

import static com.nailong.ys.ugc.sdk.utils.ByteUtils.bytebufferToByteArray;
import static com.nailong.ys.ugc.sdk.utils.ByteUtils.intToByteArray;

@Log4j2
@Service
public class EncodeService {

    public byte[] encodeGiFile(byte[] fileBytes, GiFileType fileType, String originalFileTypeStr) throws InvalidProtocolBufferException {
        GiFileModel giFileModel = new GiFileModel();

        Message.Builder protoBuilder;

        /* version */
        {
            giFileModel.setVersion(1);
        }

        /* 头部魔数 */
        {
            giFileModel.setHeadMagicNumber(ByteUtils.bytesToInt(MagicNumberConstants.HEAD_MAGIC_NUMBER));
        }

        /* 尾部魔数 */
        {
            giFileModel.setTailMagicNumber(ByteUtils.bytesToInt(MagicNumberConstants.TAIL_MAGIC_NUMBER));
        }

        /* 输出文件类型 setGiFileType */
        {
            if (fileType == GiFileType.GIL) {
                protoBuilder = UgcGilArchiveInfoBin.newBuilder();
                giFileModel.setGiFileType(GiFileType.GIL);
            } else if (fileType == GiFileType.GIA) {
                protoBuilder = UgcGiaArchiveInfoBin.newBuilder();
                giFileModel.setGiFileType(GiFileType.GIA);
            } else {
                throw new RuntimeException("error");
            }
        }

        /* setProtoMessage */
        {
            // json or pb
            if (originalFileTypeStr.startsWith("json")) {
                JsonFormat.parser().merge(new String(fileBytes), protoBuilder);
            } else {
                protoBuilder.mergeFrom(fileBytes);
            }
            giFileModel.setProtoMessage((GeneratedMessage) protoBuilder.build());
        }

        /* data size */
        {
            giFileModel.setDataLength(giFileModel.getProtoMessage().toByteArray().length);
        }

        return encodeGiFileModel(giFileModel);
    }

    public byte[] encodeGiFileModel(GiFileModel giFileModel) {
        int dataSize = giFileModel.getElementTakesUpSpace().getAllElementTakesUpSpace();

        try {
            ByteBuffer fileBuffer = ByteBuffer.allocate(dataSize + 4); // 指定容量
            {
                fileBuffer.put(intToByteArray(dataSize));
                fileBuffer.put(intToByteArray(giFileModel.getVersion()));
                fileBuffer.put(intToByteArray(giFileModel.getHeadMagicNumber()));
                fileBuffer.put(intToByteArray(giFileModel.getGiFileType().getValue()));
                fileBuffer.put(intToByteArray(giFileModel.getDataLength()));
                fileBuffer.put(giFileModel.getProtoMessage().toByteArray());
                fileBuffer.put(intToByteArray(giFileModel.getTailMagicNumber()));
            }

            return bytebufferToByteArray(fileBuffer);
        } catch (Exception e) {
            throw new RuntimeException("生成二进制数据时出错");
        }
    }
}
