package org.deepseek.service.impl;

import org.deepseek.entity.cloudbed.CloudBedDeleteResponse;
import org.deepseek.entity.cloudbed.CloudBedImageListResponse;
import org.deepseek.entity.cloudbed.CloudBedUploadResponse;
import org.deepseek.service.CloudBedService;
import org.deepseek.utils.LoggerUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CloudBedServiceImpl implements CloudBedService {

    private final WebClient webClient;
    private final String cloudBedToken;
    private final String cloudBedUrl;

    private static final int ALBUM_ID = 685;
    private static final int EXPIRE_MINUTE = 5;
    private static final String EXPIRE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private final Map<String, String> imageKeyMap = new HashMap<>();

    public CloudBedServiceImpl(WebClient.Builder webClientBuilder,
                               @Value("${cloudbed.token}") String cloudBedToken,
                               @Value("${cloudbed.url}") String cloudBedUrl) {
        this.webClient = webClientBuilder
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
        this.cloudBedToken = cloudBedToken;
        this.cloudBedUrl = cloudBedUrl;
    }

    public String uploadImgToCloudBed(File imgFile) {
        // 构建 multipart 表单
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource(imgFile));
        body.add("permission", "1");
        body.add("album_id", ALBUM_ID);
        body.add("expired_at", getExpireTime());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String expireTime = LocalDateTime.now().plusMinutes(5).format(formatter);
        body.add("expired_at", expireTime);

        // 发送请求
        Mono<String> monoResponse = webClient.post()
                .uri(cloudBedUrl + "/upload")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + cloudBedToken)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(body))
                .retrieve()
                .bodyToMono(CloudBedUploadResponse.class)
                .flatMap(response -> {
                    if (response.getStatus() &&
                            response.getData() != null &&
                            response.getData().getLinks() != null) {

                        return Mono.just(response.getData().getLinks().get("url"));
                    }
                    return Mono.error(new RuntimeException("上传失败: " + response.getMessage()));
                })
                .onErrorResume(e -> {
                    System.err.println("文件上传失败: " + e.getMessage());
                    // 如果上传失败，尝试从审核失败角度解决问题
                    // 这种情况下 图片已经上传成功，通过获取列表 间接获取图片
                    return getURLByReadingAlbum();
                });
        return monoResponse.block();
    }

    public void deleteImgFromCloudBed(String url) {
        if (imageKeyMap.containsKey(url)) {
            String key = imageKeyMap.get(url);

            String deleteUrl = cloudBedUrl + "/images/";

            Mono<String> mono = webClient.delete()
                    .uri(deleteUrl + key)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + cloudBedToken)
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .retrieve()
                    .bodyToMono(CloudBedDeleteResponse.class)
                    .flatMap(
                            response -> {
                                if (response.isStatus()) {
                                    return Mono.just(response.getMessage());
                                } else return Mono.error(new RuntimeException("删除失败: " + response.getMessage()));
                            }
                    ).onErrorResume(e ->
                            Mono.error(new RuntimeException("删除失败: " + e.getMessage()))
                    );
            LoggerUtils.info(mono.block());
        } else {
            LoggerUtils.info("图片链接不存在: " + url);
        }
    }
    private Mono<String> getURLByReadingAlbum() {
        String url = cloudBedUrl + "/images";

        return webClient.get()
                .uri(url)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + cloudBedToken)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .attribute("album_id", ALBUM_ID)
                .attribute("order", "newest")
                .attribute("permission", "public")
                .retrieve()
                .bodyToMono(CloudBedImageListResponse.class)
                .flatMap(response -> {
                    if (response.getStatus() &&
                            response.getData() != null &&
                            response.getData().getData() != null &&
                            response.getData().getData().size() > 0) {
                        List<CloudBedImageListResponse.ImageInfo> infoList = response.getData().getData();

                        CloudBedImageListResponse.ImageInfo imageInfo = infoList.get(0);
                        String imageURL = imageInfo.getUrl();
                        imageKeyMap.put(imageURL, imageInfo.getKey());

                        return Mono.just(imageURL);
                    }
                    return Mono.error(new RuntimeException("获取图片链接失败: " + response.getMessage()));
                })
                .onErrorResume(e ->
                        Mono.error(new RuntimeException("获取图片链接失败: " + e.getMessage()))
                );
    }

    private String getExpireTime() {
        return LocalDateTime.now().plusMinutes(EXPIRE_MINUTE).format(DateTimeFormatter.ofPattern(EXPIRE_TIME_FORMAT));
    }
}