package org.deepseek.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;

public interface ImageService {
    boolean generateImage(String message);
    String resolveImage(String message, MultipartFile file);
    String resolveImage(String message, String imageURL);


}
