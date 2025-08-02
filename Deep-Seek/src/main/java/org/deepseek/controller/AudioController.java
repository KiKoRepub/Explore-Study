package org.deepseek.controller;

import org.deepseek.service.AudioService;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiAudioSpeechModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.audio.speech.SpeechPrompt;
import org.springframework.ai.openai.audio.speech.SpeechResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

@RestController
@RequestMapping("/audio")
public class AudioController extends AIController{

    @Autowired
    private AudioService audioService;



    @PostMapping("/generate")
    public ResponseEntity<Boolean> generateAudio(@RequestParam("message") String message){

        return ResponseEntity.ok(audioService.generateAudio(message));
    }

    public static void main(String[] args) {
        String audioPath = "audios/audio.wav";
        InputStream resourceAsStream = AudioController.class.getClassLoader().getResourceAsStream(audioPath);

        playAudio(resourceAsStream);
    }

    private static void playAudio(String audioPath)  {

        try(AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(audioPath))) {
            Clip clip = AudioSystem.getClip();

            clip.open(audioInputStream);
            clip.start();
            System.out.println("播放中...");

            // 等待音频播放完成
            while (!clip.isRunning()) Thread.sleep(10);
            while (!clip.isRunning()) Thread.sleep(10);

            System.out.println("播放完成！");
        } catch (LineUnavailableException | UnsupportedAudioFileException | IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    private static void playAudio(InputStream stream)  {

        try(AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(stream)) {
            Clip clip = AudioSystem.getClip();

            clip.open(audioInputStream);
            clip.start();
        } catch (LineUnavailableException | UnsupportedAudioFileException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
