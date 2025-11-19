package org.dee.service.impl;

import org.dee.entity.vo.ImageGenerateResult;
import org.dee.service.ImageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DashScopeImageService implements ImageService {

    @Override
    public ImageGenerateResult generateImage(String message) {
        return null;
    }

    @Override
    public String resolveImage(String message, MultipartFile file) {
        return null;
    }

    @Override
    public String resolveImage(String message, String imageURL) {
        return null;
    }
}
