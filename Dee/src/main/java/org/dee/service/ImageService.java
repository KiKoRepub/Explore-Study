package org.dee.service;

import org.springframework.web.multipart.MultipartFile;

public interface ImageService {

    boolean generateImage(String message);
    String resolveImage(String message, MultipartFile file);
    String resolveImage(String message, String imageURL);


}