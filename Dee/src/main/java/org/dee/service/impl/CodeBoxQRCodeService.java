package org.dee.service.impl;


import org.dee.config.QRCodeProperties;
import org.dee.service.QRCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.Serializable;

/*
 * Url: https://www.codebox.club/
 */
@Service
public class CodeBoxQRCodeService implements QRCodeService {

    @Autowired
    private QRCodeProperties qrCodeProperties;


    @Override
    public String generateQRCode(String text, int width, int height) {
        // TODO： 生成二维码(没有具体的API)
        return null;
    }

    @Override
    public Object getQRCodeInfo(Serializable id) {
        // TODO: 获取二维码信息
        //GET /api/v1/qrcode/{id}
        //cb-api-key: xxxxxxxxxx

        Object result = WebClient.builder()
                .baseUrl(qrCodeProperties.getBaseUrl() + id)
                .defaultHeader("cb-api-key", qrCodeProperties.getApiKey())
                .defaultHeader("Content-Type", "application/json")
                .build()
                .get()
                .retrieve()
                .bodyToMono(Object.class)
                .block();


        return result;
    }
}
