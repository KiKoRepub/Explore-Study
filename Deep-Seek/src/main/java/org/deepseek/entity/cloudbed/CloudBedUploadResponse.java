package org.deepseek.entity.cloudbed;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
public class CloudBedUploadResponse {
    private Boolean status;

    /**
     * 描述信息
     */
    private String message;

    /**
     * 图片数据对象
     */
    private ImageData data;

    /**
     * 图片数据实体
     */
    @Data
    public static class ImageData {

        /**
         * 图片唯一密钥
         */
        private String key;

        /**
         * 图片名称
         */
        private String name;

        /**
         * 图片路径名
         */
        private String pathname;

        /**
         * 图片原始名
         */
        @JsonProperty("origin_name")
        private String originName;

        /**
         * 图片大小(KB)
         */
        private Float size;

        /**
         * 图片MIME类型
         */
        private String mimetype;

        /**
         * 图片扩展名
         */
        private String extension;

        /**
         * 图片MD5值
         */
        private String md5;

        /**
         * 图片SHA1值
         */
        private String sha1;

        /**
         * 图片链接集合
         */
        private Map<String, String> links;
    }

}
