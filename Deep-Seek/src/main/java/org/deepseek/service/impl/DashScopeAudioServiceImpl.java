package org.deepseek.service.impl;

import com.alibaba.cloud.ai.dashscope.audio.DashScopeSpeechSynthesisModel;
import com.alibaba.cloud.ai.dashscope.audio.DashScopeSpeechSynthesisOptions;
import com.alibaba.cloud.ai.dashscope.audio.synthesis.SpeechSynthesisPrompt;
import com.alibaba.cloud.ai.dashscope.audio.synthesis.SpeechSynthesisResponse;

import org.deepseek.entity.ResourceType;
import org.deepseek.service.AudioService;
import org.deepseek.utils.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

@Service
public class DashScopeAudioServiceImpl implements AudioService {

    @Autowired
    DashScopeSpeechSynthesisModel speechModel;

    @Override
    public boolean generateAudio(String text) {
        DashScopeSpeechSynthesisOptions options = DashScopeSpeechSynthesisOptions.builder()
                .build();

        SpeechSynthesisResponse response = speechModel.call(
                new SpeechSynthesisPrompt(text)
        );


        System.out.println(response);
        ByteBuffer audioBuffer = response.getResult().getOutput().getAudio();

        return FileUtils.downloadFromBuffer(audioBuffer,
                "dashscope_"+"%.2f".formatted(10* Math.random())
                , ResourceType.AUDIO);
    }
}
