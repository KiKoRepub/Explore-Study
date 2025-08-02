package org.deepseek.controller;

import ai.z.openapi.ZhipuAiClient;
import ai.z.openapi.service.model.*;
import org.deepseek.service.CloudBedService;
import org.deepseek.service.ImageService;
import org.deepseek.utils.FileUtils;
import org.deepseek.utils.LoggerUtils;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.ai.zhipuai.ZhiPuAiChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URI;
import java.util.*;

@RestController
@RequestMapping("/image")
public class ImageController extends AIController {



    @Autowired
//    @Qualifier("zhiPuAiImageServiceImpl")
    @Qualifier("dashScopeImageServiceImpl")
    private ImageService imageService;


    private static final String DEFAULT_MESSAGE = "你从这张图片中看到了什么？";

    @PostMapping("/generate")
    public ResponseEntity<Boolean> generateImage(@RequestParam("message") String message) {

        boolean res =imageService.generateImage(message);

        return ResponseEntity.ok().body(res);
    }

    @PostMapping("/resolve-file")
    public ResponseEntity<String> resolveImage(@RequestParam("image") MultipartFile file,
                                               @RequestParam(value = "message", required = false, defaultValue = DEFAULT_MESSAGE) String message
                                               ) {

        return ResponseEntity.ok(imageService.resolveImage(message, file));
    }
    @PostMapping("/resolve-url")
    public ResponseEntity<String> resolveImage(@RequestParam("url") String url,
                                               @RequestParam(value = "message", required = false, defaultValue = DEFAULT_MESSAGE) String message) {
        return ResponseEntity.ok(imageService.resolveImage(message,url));

    }


}
