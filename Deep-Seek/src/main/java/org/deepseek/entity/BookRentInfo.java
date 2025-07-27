package org.deepseek.entity;

import lombok.Data;


public record BookRentInfo(
        String bookName,
        String shopName,
        int stock,
        String rentPrice,
        String canRentTime

) {


}
