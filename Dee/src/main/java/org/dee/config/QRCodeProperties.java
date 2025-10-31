package org.dee.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "qrcode")
public class QRCodeProperties {


    private String apiKey;
    private String baseUrl;



}
