package org.deepseek.service.impl;

import ai.z.openapi.ZhipuAiClient;
import ai.z.openapi.service.model.*;
import okhttp3.*;
import org.deepseek.entity.ResourceType;
import org.deepseek.service.CloudBedService;
import org.deepseek.service.ImageService;
import org.deepseek.utils.FileUtils;
import org.deepseek.utils.LoggerUtils;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class ImageServiceImpl implements ImageService {

    @Value("${extra.zhipu-image.resolve}")
    private String imageResolveModelName;

    @Autowired
    ImageModel imageModel;
    @Autowired
    protected ZhipuAiClient zhipuAiClient;
    @Autowired
    private CloudBedService cloudBedService;

    @Override
    public boolean generateImage(String message) {
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
            res &= FileUtils.downloadFromURL(url, "test" + Math.random() * 10, ResourceType.IMAGE);
        }
        return res;
    }
    @Override
    public String resolveImage(String message, MultipartFile file) {
        // 先保存到本地，等待正式的上传
        File fileSaved = FileUtils.saveRequestImageFile(file);

        if (fileSaved == null) return "文件上传失败";
        // 上传后 返回 对应的图片链接
        String imageURL = cloudBedService.uploadImgToCloudBed(fileSaved);

        String content = getResolveResult(message, imageURL);

        // 删除 相关文件
        fileSaved.delete();
        cloudBedService.deleteImgFromCloudBed(imageURL);

        return content;
    }

    @Override
    public String resolveImage(String message, String imageURL) {
        try {
            return getResolveResult(message, imageURL);
        } catch (Exception e) {
            LoggerUtils.error(e);
            return "解析失败";
        }
    }



    private String getResolveResult(String message, String imageURL) {

        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model(imageResolveModelName)
                .messages(Collections.singletonList(
                        ChatMessage.builder()
                                .role(ChatMessageRole.USER.value())
                                .content(Arrays.asList(
                                        MessageContent.builder()
                                                .type("text")
                                                .text(message)
                                                .build(),
                                        MessageContent.builder()
                                                .type("image_url")
                                                .imageUrl(ImageUrl.builder()
                                                        .url(imageURL)
                                                        .build())
                                                .build()))
                                .build()
                )).build();

        ChatCompletionResponse response = zhipuAiClient.chat()
                .createChatCompletion(params);
        if (response.isSuccess()) {
            Object reply = response.getData().getChoices()
                    .get(0).getMessage()
                    .getContent();

            System.out.println(reply);
            return reply.toString();
        }

        return null;
    }

}
