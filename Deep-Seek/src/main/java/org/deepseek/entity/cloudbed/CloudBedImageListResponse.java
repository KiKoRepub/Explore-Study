package org.deepseek.entity.cloudbed;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

/**
 * 图片列表分页响应
 */
@Data
public class CloudBedImageListResponse {

    /**
     * 请求状态
     */
    private Boolean status;

    /**
     * 描述信息
     */
    private String message;

    /**
     * 分页数据
     */
    private PagedImageData data;


    /**
     * 分页数据实体
     */
    @Data
    public static class PagedImageData {

        /**
         * 当前页码
         */
        @JsonProperty("current_page")
        private Integer currentPage;

        /**
         * 最后一页页码
         */
        @JsonProperty("last_page")
        private Integer lastPage;

        /**
         * 每页数据量
         */
        @JsonProperty("per_page")
        private Integer perPage;

        /**
         * 图片总数
         */
        private Integer total;

        /**
         * 图片数据列表
         */
        private List<ImageInfo> data;

    }
    /**
     * 图片信息实体
     */
    @Data
    public static class ImageInfo {

        /**
         * 图片唯一密钥
         */
        private String key;

        /**
         * 图片名称
         */
        private String name;

        /**
         * 图片原始名称
         */
        @JsonProperty("origin_name")
        private String originName;

        /**
         * 图片路径名
         */
        private String pathname;

        /**
         * 图片大小(KB)
         */
        private Float size;

        /**
         * 图片宽度(像素)
         */
        private Integer width;

        /**
         * 图片高度(像素)
         */
        private Integer height;

        /**
         * 图片MD5值
         */
        private String md5;

        /**
         * 图片SHA1值
         */
        private String sha1;

        /**
         * 友好格式的上传时间
         */
        @JsonProperty("human_date")
        private String humanDate;

        /**
         * 标准格式的上传时间 (yyyy-MM-dd HH:mm:ss)
         */
        private String date;

        /**
         * 图片链接集合
         */
        private Map<String, String> links;


        /**
         * 链接快捷访问方法
         */
        public String getUrl() {
            return links != null ? links.get("url") : null;
        }

        public String getThumbnailUrl() {
            return links != null ? links.get("thumbnail_url") : null;
        }

        public String getDeleteUrl() {
            return links != null ? links.get("delete_url") : null;
        }
    }
}