package org.deepseek.entity.zhipu;


public record VideoGeneratingResponse(
    String model,
    String id,
    String requestId,
    String taskStatus
){

}
