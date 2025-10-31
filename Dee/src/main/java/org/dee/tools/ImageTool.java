package org.dee.tools;

import org.dee.annotions.MyTool;
import org.dee.entity.ImageGenerateResult;
import org.springframework.ai.tool.annotation.Tool;

@MyTool("图像处理工具")
public class ImageTool {


    @Tool
    public ImageGenerateResult generateImage(String prompt) {

        return new ImageGenerateResult("https://example.com/image.png",null,null);
    }
}
