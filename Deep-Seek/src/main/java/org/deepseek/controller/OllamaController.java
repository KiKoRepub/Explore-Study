package org.deepseek.controller;

import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OllamaController {
    @Autowired
    OllamaChatModel ollamaChatModel;
}
