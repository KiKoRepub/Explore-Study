package org.dee.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@Deprecated
@ConfigurationProperties(prefix = "zhipu") // ← 注意是 "zhipu"，不是 "zhipuai"
public class ZhiPuAIProperties {

    private Image image = new Image();
    private Chat chat = new Chat();
    private Video video = new Video();
    private Completion completion = new Completion();

    // Getters and Setters

    public static class Image {
        private String resolve;

        public String getResolve() { return resolve; }
        public void setResolve(String resolve) { this.resolve = resolve; }
    }

    public static class Chat {
        private String unknown;

        public String getUnknown() { return unknown; }
        public void setUnknown(String unknown) { this.unknown = unknown; }
    }

    public static class Video {
        private String apiKey;  // 对应 yaml 中的 api-key（Spring 会自动转为驼峰）
        private String model;

        public String getApiKey() { return apiKey; }
        public void setApiKey(String apiKey) { this.apiKey = apiKey; }

        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }
    }

    public static class Completion {
        private String apiKey;

        public String getApiKey() { return apiKey; }
        public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    }

}