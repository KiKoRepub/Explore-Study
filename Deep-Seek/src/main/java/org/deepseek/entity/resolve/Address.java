package org.deepseek.entity.resolve;

import lombok.Data;

@Data
public class Address {

    String consigneeName; // 收货人
    String phone; // 手机号
    String province; // 省份
    String city; //  城市
    String distract; // 区县
    String detail; // 详细地址

}
