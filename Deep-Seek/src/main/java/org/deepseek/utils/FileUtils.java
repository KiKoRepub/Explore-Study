package org.deepseek.utils;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.deepseek.entity.ResourceType;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

public class FileUtils {
    private static final String RESOURCES_PATH = "D:\\university\\JAVA\\Explore-Study\\Deep-Seek\\src\\main\\resources";

    private static final String IMG_PATH = RESOURCES_PATH + "/images";
    private static final String VIDEO_PATH = RESOURCES_PATH + "/videos";
    private static final String AUDIO_PATH = RESOURCES_PATH + "/audios";

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

    public static boolean downloadFromURL(String fileUrl, String fileName, ResourceType type) {
        // 构建 保存后的文件路径
        String suffix = fileUrl.substring(fileUrl.lastIndexOf("."));
        String saving = "";
        if (type == ResourceType.IMAGE)
             saving = IMG_PATH + "\\" + fileName + suffix;
        if (type == ResourceType.VIDEO)
             saving = VIDEO_PATH + "\\" + fileName + suffix;


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


    public static void main(String[] args) {
//        String url = "https://aigc-files.bigmodel.cn/api/cogvideo/090ca92e-6eb2-11f0-af25-fe252d325a87_0.mp4";
        String url = "https://aigc-files.bigmodel.cn/api/cogvideo/6954cc8a-6ec6-11f0-b4d3-fecd4373136c_0.mp4";
        downloadFromURL(url,"video2",ResourceType.VIDEO);
    }

}
