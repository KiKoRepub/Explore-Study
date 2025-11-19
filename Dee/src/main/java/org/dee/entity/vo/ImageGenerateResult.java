package org.dee.entity.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ImageGenerateResult {

    @ApiModelProperty("Image URL")
    private String imageUrl;

    @ApiModelProperty("Base64 Encoded Image")
    private String imageBase64;
    @ApiModelProperty("Image Name")
    private String imageName;


    public ImageGenerateResult(String imageUrl, String imageBase64, String imageName) {
        this.imageUrl = imageUrl;
        this.imageBase64 = imageBase64;
        this.imageName = imageName;
    }
}
