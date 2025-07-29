package org.deepseek.controller;

import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/video")
public class VideoController extends AIController{


    public VideoController(OpenAiChatModel openAiChatModel) {
        super(openAiChatModel);
    }




}
