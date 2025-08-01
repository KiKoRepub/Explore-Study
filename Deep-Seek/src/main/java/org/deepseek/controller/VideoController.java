package org.deepseek.controller;

import com.alibaba.fastjson.JSONObject;
import org.deepseek.entity.zhipu.VideoGeneratingResponse;
import org.deepseek.service.VideoService;
import org.deepseek.utils.FileUtils;
import org.deepseek.utils.LoggerUtils;
import org.jetbrains.annotations.Nullable;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.Serializable;
import java.util.Map;

@RestController
@RequestMapping("/video")
public class VideoController extends AIController {

    @Autowired
    private  VideoService videoService;


    public VideoController(OpenAiChatModel openAiChatModel) {
        super(openAiChatModel);
    }


    @PostMapping("/generate")
    public ResponseEntity<Boolean> generateVideo(@RequestParam("message") String message) {

        String taskId = videoService.requestForVideoGenerate(message);

//        1091753691400010-8471984102537400777
        LoggerUtils.info("视频正在生成中 对应的 任务id 为 %s",taskId);



        boolean result = videoService.getGenerateResult(taskId);


        return ResponseEntity.ok(result);

    }

    @GetMapping("/generate-result")
    public ResponseEntity<Boolean> getGenerateResult(@RequestParam("taskId") String taskId) {
        return ResponseEntity.ok(videoService.getGenerateResult(taskId));
    }

}
