package org.dee.entity;

import lombok.Data;

@Data
public class ImageGenerateResult {


    private String imageUrl;
    private String imageBase64;
    private String imageName;


    public ImageGenerateResult(String imageUrl, String imageBase64, String imageName) {
        this.imageUrl = imageUrl;
        this.imageBase64 = imageBase64;
        this.imageName = imageName;
    }
}
