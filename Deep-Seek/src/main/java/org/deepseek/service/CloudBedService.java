package org.deepseek.service;

import reactor.core.publisher.Mono;

import java.io.File;

public interface CloudBedService {

    String uploadImgToCloudBed(File imgFile);
    void deleteImgFromCloudBed(String url);

}
