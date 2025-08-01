package org.deepseek.service.impl;

import org.deepseek.entity.ResourceType;
import org.deepseek.entity.zhipu.VideoGeneratingResponse;
import org.deepseek.service.VideoService;
import org.deepseek.utils.FileUtils;
import org.deepseek.utils.LoggerUtils;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class VideoServiceImpl implements VideoService {


    @Value("${extra.zhipu-video.model}")
    private String videoModelName;
    @Value("${extra.zhipu-video.api-key}")
    private String apiKey;

    private final WebClient webClient;

    public VideoServiceImpl(WebClient.Builder webBuilder) {
        this.webClient = webBuilder.build();
    }

    @Nullable
    public String requestForVideoGenerate(String message) {
        /*
             curl --request POST \
                  --url https://open.bigmodel.cn/api/paas/v4/videos/generations \
                  --header 'Authorization: Bearer <token>' \
                  --header 'Content-Type: application/json' \
                  --data '{
                      "prompt": "<string>",
                      "model": "cogvideox-3"
                  }
       */

        String url = "https://open.bigmodel.cn/api/paas/v4/videos/generations";


        Map<String, Object> paramBody = Map.of(
                "model", videoModelName,
                "prompt", message
        );


        Mono<String> mono = webClient.post()
                .uri(url)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .body(BodyInserters.fromValue(paramBody))
                .retrieve()
                .bodyToMono(VideoGeneratingResponse.class)
                .flatMap(response -> {
                    if (response != null) {
                        return Mono.just(response.id());
                    }
                    return Mono.error(new RuntimeException("请求失败"));
                }).onErrorResume(e -> {
                    System.err.println("请求失败：" + e.getMessage());
                    return Mono.error(new RuntimeException("请求失败"));
                });

        return mono.block();
    }


    public static void main(String[] args) {



    }


    public boolean getGenerateResult(String taskId) {
        // 轮询， 查询实际状态

        /*
         curl --request GET \
              --url https://open.bigmodel.cn/api/paas/v4/async-result/{id} \
              --header 'Authorization: Bearer <token>'
         */


        String reqUrl = "https://open.bigmodel.cn/api/paas/v4/async-result/" + taskId;

        String videoUrl = "";

        while (true) {
            LoggerUtils.info("正在查询视频生成状态，url: %s", reqUrl);

            Mono<? extends Serializable> resultMono = webClient.get()
                    .uri(reqUrl)
                    .header("Authorization", "Bearer " + apiKey)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .flatMap(json -> {
                        if (json != null &&
                                json.get("video_result") != null) {
                            Object videoResult = json.get("video_result");

                            if (videoResult instanceof List<?>  resultList ) {
                                if (resultList.get(0) instanceof  Map<?, ?> resultMap) {
                                    if (resultMap.get("url") != null) {
                                        return Mono.just(resultMap.get("url").toString());
                                    }
                                }
                            }

                        }
                        return Mono.just(false);
                    }).onErrorResume(e -> {
                        System.err.println("请求失败：" + e.getMessage());
                        return Mono.just(false);
                    });

            Serializable block = resultMono.block();

            if (block instanceof String) {
                videoUrl = block.toString();
                break;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        String videoName = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")) + "_video";
        return FileUtils.downloadFromURL(videoUrl, videoName, ResourceType.VIDEO);
    }
}
