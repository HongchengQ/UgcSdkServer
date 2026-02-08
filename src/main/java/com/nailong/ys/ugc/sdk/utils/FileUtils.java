package com.nailong.ys.ugc.sdk.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public final class FileUtils {
    /**
     * 把一个文件转化为byte字节数组。
     */
    public static byte[] fileConvertToByteArray(File file) throws IOException {
        byte[] data;

        FileInputStream fis = new FileInputStream(file);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        int len;
        byte[] buffer = new byte[1024];
        while ((len = fis.read(buffer)) != -1) {
            baos.write(buffer, 0, len);
        }

        data = baos.toByteArray();

        fis.close();
        baos.close();

        return data;
    }

    public static File getUerdirFile(String path) {
        String fullPath = System.getProperty("user.dir") + File.separator + path;
        return new File(fullPath);
    }

    /**
     * 使用Files.walk方法查找文件
     *
     * @param directoryPath 目录路径
     * @param extensions    文件后缀数组
     * @return 符合条件的文件列表
     */
    public static List<File> findFilesByExtension(String directoryPath, String[] extensions) {
        List<File> result = new ArrayList<>();

        try (Stream<Path> walk = Files.walk(Paths.get(directoryPath))) {
            List<Path> paths = walk
                    .filter(Files::isRegularFile)
                    .filter(path -> matchesExtension(path.toString(), extensions))
                    .toList();

            // 转换为File对象
            for (Path path : paths) {
                result.add(path.toFile());
            }
        } catch (IOException e) {
            System.err.println("遍历目录时出错: " + e.getMessage());
        }

        return result;
    }

    private static boolean matchesExtension(String fileName, String[] extensions) {
        if (extensions == null || extensions.length == 0) {
            return true;
        }

        for (String extension : extensions) {
            if (fileName.toLowerCase().endsWith(extension.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

}
