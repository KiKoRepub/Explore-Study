package org.deepseek.service;

public interface VideoService {
    String requestForVideoGenerate(String message);

    boolean getGenerateResult(String taskId);
}
