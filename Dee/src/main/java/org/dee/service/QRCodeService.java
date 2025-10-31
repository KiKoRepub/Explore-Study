package org.dee.service;

import java.io.Serializable;

public interface QRCodeService {


    String generateQRCode(String text, int width, int height);


    Object getQRCodeInfo(Serializable id);
}
