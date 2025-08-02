package org.deepseek.service.impl;


import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.dashscope.image.DashScopeImageModel;
import com.alibaba.cloud.ai.dashscope.image.DashScopeImageOptions;
import org.deepseek.entity.ResourceType;
import org.deepseek.service.ImageService;
import org.deepseek.utils.FileUtils;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.ai.deepseek.DeepSeekChatOptions;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@Component
public class DashScopeImageServiceImpl implements ImageService {

    @Value("${extra.dashscope-image.resolve}")
    private   String imageResolveModelName;

    @Autowired
    DashScopeImageModel imageModel;

    @Autowired
    DashScopeChatModel chatModel; // 用来图片识别

    private static final String IMAGE_PREFIX = "dashscope_";

    @Override
    public boolean generateImage(String message) {

        ImagePrompt prompt = new ImagePrompt(message);

        ImageResponse response = imageModel.call(prompt);

        String url = response.getResult().getOutput().getUrl();

        System.out.println(url);
        return FileUtils.downloadFromURL(url,
                IMAGE_PREFIX + "%.2f".formatted(Math.random() * 10),
                ResourceType.IMAGE);

    }

    @Override
    public String resolveImage(String message, MultipartFile file) {

//        file.getResource();
        Media media = new Media(MimeTypeUtils.IMAGE_PNG,file.getResource());

        DashScopeChatOptions options = DashScopeChatOptions.builder()
                .withModel(imageResolveModelName)
                .withMultiModel(true)
                .build();

        Prompt prompt = Prompt.builder()
                .chatOptions(options)
                .messages(UserMessage.builder().media(media)
                        .text("你从这张图片中看到了哪些内容？如果是人物，认得出来是谁吗")
                        .build())
                .build();

        ChatResponse response = chatModel.call(prompt);

        String result = response.getResult().getOutput().getText();
        System.out.println(result);


        return result;
    }

    @Override
    public String resolveImage(String message, String imageURL) {




        return null;
    }
}
