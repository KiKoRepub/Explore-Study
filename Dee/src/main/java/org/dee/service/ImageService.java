package org.dee.service;

import org.dee.entity.vo.ImageGenerateResult;
import org.springframework.web.multipart.MultipartFile;

public interface ImageService {

    ImageGenerateResult generateImage(String message);
    String resolveImage(String message, MultipartFile file);
    String resolveImage(String message, String imageURL);


}