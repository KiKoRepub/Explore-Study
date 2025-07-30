package org.deepseek.utils;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

public class FileUtils {
    private static final String RESOURCES_PATH = "D:\\university\\JAVA\\Explore-Study\\Deep-Seek\\src\\main\\resources";

    private static final String IMG_PATH = RESOURCES_PATH + "/images";

    public static File saveRequestImageFile(MultipartFile file) {
        File saveDir = new File(IMG_PATH);

        if (!saveDir.exists() && !saveDir.mkdirs()) {
            LoggerUtils.error("创建目录失败", new IOException("创建目录失败"));
        }

        String fileReferName = file.getOriginalFilename();
        String referSuffix = fileReferName.substring(fileReferName.lastIndexOf("."));
        String targetFileName = UUID.randomUUID().toString() + referSuffix;


        File targetFile = new File(IMG_PATH, targetFileName);
        try {
            // 创建文件 并 拷贝
            if (targetFile.createNewFile()) {
                FileOutputStream out = new FileOutputStream(targetFile);

                IOUtils.copy(file.getInputStream(), out);

                out.close();
            } else throw new IOException("文件创建失败");
        } catch (IOException e) {
            LoggerUtils.error(e);
            return null;
        }
        return targetFile;

    }

    public static boolean downloadFromURL(String fileUrl, String fileName) {
        // 构建 保存后的文件路径
        String suffix = fileUrl.substring(fileUrl.lastIndexOf("."));
        String saving = IMG_PATH + "\\" + fileName + suffix;


        try (FileOutputStream out = new FileOutputStream(saving)) {


            Resource resource = new UrlResource(fileUrl);

            if (!resource.exists()) {
                throw new IOException("文件不存在: " + fileUrl);
            }


            StreamUtils.copy(resource.getInputStream(), out);
            System.out.println("下载完成，路径为: " + saving);
            return true;
        } catch (IOException e) {

            LoggerUtils.error(e);
            return false;
        }
    }

}
