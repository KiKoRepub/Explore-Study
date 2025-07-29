package org.deepseek.controller;

import org.deepseek.utils.LoggerUtils;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

@RestController
@RequestMapping("/image")
public class ImageController extends AIController {




    public ImageController(OpenAiChatModel openAiChatModel) {
        super(openAiChatModel);
    }

    @GetMapping("/generate")
    public ResponseEntity<Boolean> generateImage(@RequestParam("message") String message) {

        ImageResponse response = imageModel.call(
                new ImagePrompt(message,
                        OpenAiImageOptions.builder()
                                .height(1024)
                                .width(1024)
                                .build()
                    )
        );
        List<String> urls = response.getResults().stream()
                .map(imageGenerate -> imageGenerate.getOutput().getUrl())
                .toList();
        boolean res = true;
        for (String url : urls) {
            System.out.println(url);
            res &= downloadWithRestTemplate(url,
                    "D:\\university\\JAVA\\Explore-Study\\Deep-Seek\\src\\main\\resources",
                    "test"+Math.random()*10);
        }

        return ResponseEntity.ok().body(res);
    }


//    public static void main(String[] args) {
//        String url = "https://aigc-files.bigmodel.cn/api/cogview/2025072817135617989acc23f54dcc_0.png";
//        String savePath = "D:\\university\\JAVA\\Explore-Study\\Deep-Seek\\src\\main\\resources";
//        downloadWithRestTemplate(url, savePath,"test1");
//    }

    public static boolean downloadWithRestTemplate(String fileUrl, String savePath,String fileName)  {
        // 构建 保存后的文件路径
        String suffix = fileUrl.substring(fileUrl.lastIndexOf("."));
        String saving = savePath + "\\"+fileName + suffix;


        try (FileOutputStream out = new FileOutputStream(saving)) {


        Resource resource = new UrlResource(fileUrl);

        if (!resource.exists()) {
            throw new IOException("文件不存在: " + fileUrl);
        }


            StreamUtils.copy(resource.getInputStream(), out);
            System.out.println("RestTemplate 下载完成: " + saving);
            return true;
        }catch (IOException e){

            LoggerUtils.error(e);
            return false;
        }
    }
}
