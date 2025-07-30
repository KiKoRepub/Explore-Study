package org.deepseek.controller;

import ai.z.openapi.ZhipuAiClient;
import ai.z.openapi.service.model.*;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.deepseek.utils.LoggerUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.zhipuai.ZhiPuAiChatOptions;
import org.springframework.ai.zhipuai.api.ZhiPuAiApi;
import org.springframework.ai.zhipuai.api.ZhiPuAiImageApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeTypeUtils;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URI;
import java.util.*;

@RestController
@RequestMapping("/image")
public class ImageController extends AIController {

    @Value("${extra.zhipu-image.resolve}")
    private String imageResolveModelName;
    @Autowired
    ImageModel imageModel;

    @Autowired
    protected ZhipuAiClient zhipuAiClient;

    private static final String RESOURCES_PATH = "D:\\university\\JAVA\\Explore-Study\\Deep-Seek\\src\\main\\resources";

    public ImageController(OpenAiChatModel openAiChatModel) {
        super(openAiChatModel);
    }

    @PostMapping("/generate")
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
                    RESOURCES_PATH, "test" + Math.random() * 10);
        }

        return ResponseEntity.ok().body(res);
    }


    @PostMapping("/resolve-file")
    public ResponseEntity<String> resolveImage(@RequestParam("image") MultipartFile file,
                                               @RequestParam(value = "message", required = false, defaultValue = "") String message,
                                               @RequestParam(value = "filePath",defaultValue = "https://aigc-files.bigmodel.cn/api/cogview/20250723213827da171a419b9b4906_0.png")String filePath) throws IOException {

//        String filePath = saveRequestFile(file);


        if (filePath == null) return ResponseEntity.badRequest().body("文件上传失败");

        String content = getResolveResult3(message, filePath);
        return ResponseEntity.ok(content);
    }

    @NotNull
    private String getResolveResult1(String message, String filePath) {
        message = Objects.equals(message, "")
                ? "你从这张图片中看到了什么？"
                : message;

        var imageResource = new FileSystemResource(filePath);
        var userMessage = UserMessage.builder()
                .text(message)
                .media(List.of(Media.builder()
                        .mimeType(MimeTypeUtils.ALL)
                        .data(imageResource)
                        .build()))
                .build();

        ChatResponse response = chatClient.prompt(new Prompt(userMessage,
                        ZhiPuAiChatOptions.builder()
                                .model(imageResolveModelName)
                                .build()))
                .call()
                .chatResponse();

        System.out.println(response);
        System.out.println(response.getResult());
        System.out.println(response.getResult().getOutput());
        System.out.println(response.getResult().getOutput().getText());

        String content = response.getResult().getOutput().getText();
        return content;
    }

    private String getResolveResult2(String message, MultipartFile file) throws IOException {
        message = Objects.equals(message, "")
                ? "你从这张图片中看到了什么？"
                : message;


        String base64Image = "data:image/png;base64," + Base64.getEncoder().encodeToString(file.getBytes());


        Media media = new Media(MimeTypeUtils.IMAGE_PNG, URI.create(base64Image));

        var userMessage = UserMessage.builder()
                .text(message)
                .media(List.of(media))
                .build();
        ZhiPuAiApi.ChatCompletionMessage message1 = new ZhiPuAiApi.ChatCompletionMessage(
                message,
                ZhiPuAiApi.ChatCompletionMessage.Role.USER
        );

        ZhiPuAiApi.ChatCompletionRequest request = new ZhiPuAiApi.ChatCompletionRequest(
                Collections.singletonList(message1),
                imageResolveModelName,
                0.5
        );


        ChatResponse response = chatClient.prompt(new Prompt(userMessage,
                        ZhiPuAiChatOptions.builder()
                                .model(imageResolveModelName)
                                .build()))
                .call()
                .chatResponse();

        System.out.println(response);
        System.out.println(response.getResult());
        System.out.println(response.getResult().getOutput());
        System.out.println(response.getResult().getOutput().getText());

        String content = response.getResult().getOutput().getText();
        return content;
    }

    private String getResolveResult3(String message, String filePath) {

        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model(imageResolveModelName)
                .messages(Arrays.asList(
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
                                                        .url(filePath)
                                                        .build())
                                                .build()))
                                .build()
                )).build();

        ChatCompletionResponse response = zhipuAiClient.chat()
                .createChatCompletion(params);
        if (response.isSuccess()){
            Object reply = response.getData().getChoices()
                    .get(0).getMessage()
                    .getContent();

            System.out.println(reply);
            return reply.toString();
        }

        return null;
    }

    @PostMapping("/resolve-url")
    public ResponseEntity<String> resolveImage(@RequestParam("url") String url,
                                               @RequestParam(value = "message", required = false, defaultValue = "") String message) {

        message = Objects.equals(message, "")
                ? "你从这张图片中看到了什么？"
                : message;

        URI imageURI = null;

        try {
            imageURI = URI.create(url);
        } catch (Exception e) {
            LoggerUtils.error(e);
            return ResponseEntity.badRequest().body("url不合法");
        }

        var userMessage = UserMessage.builder()
                .text(message)
                .media(List.of(Media.builder()
                        .mimeType(MimeTypeUtils.ALL)
                        .data(imageURI)
                        .build()))
                .build();


        String content = chatClient.prompt(new Prompt(userMessage,
                        ZhiPuAiChatOptions.builder().model(imageResolveModelName).build()))
                .call()
                .content();

        return ResponseEntity.ok(content);


    }

    private String saveRequestFile(MultipartFile file) {
        String savePath = RESOURCES_PATH + "/images";
        File saveDir = new File(savePath);

        if (!saveDir.exists() && !saveDir.mkdirs()) {
            LoggerUtils.error("创建目录失败", new IOException("创建目录失败"));
        }

        String fileReferName = file.getOriginalFilename();
        String referSuffix = fileReferName.substring(fileReferName.lastIndexOf("."));
        String targetFileName = UUID.randomUUID().toString() + referSuffix;


        File targetFile = new File(savePath, targetFileName);
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
        return targetFile.getAbsolutePath();

    }

//    public static void main(String[] args) {
//        String url = "https://aigc-files.bigmodel.cn/api/cogview/2025072817135617989acc23f54dcc_0.png";
//        String savePath = "D:\\university\\JAVA\\Explore-Study\\Deep-Seek\\src\\main\\resources";
//        downloadWithRestTemplate(url, savePath,"test1");
//    }

    public static boolean downloadWithRestTemplate(String fileUrl, String savePath, String fileName) {
        // 构建 保存后的文件路径
        String suffix = fileUrl.substring(fileUrl.lastIndexOf("."));
        String saving = savePath + "\\" + fileName + suffix;


        try (FileOutputStream out = new FileOutputStream(saving)) {


            Resource resource = new UrlResource(fileUrl);

            if (!resource.exists()) {
                throw new IOException("文件不存在: " + fileUrl);
            }


            StreamUtils.copy(resource.getInputStream(), out);
            System.out.println("RestTemplate 下载完成: " + saving);
            return true;
        } catch (IOException e) {

            LoggerUtils.error(e);
            return false;
        }
    }
}
