package org.deepseek.entity.cloudbed;

import lombok.Data;

@Data
public class CloudBedDeleteResponse {

    private boolean status;
    private String message;
    private Object data;


}
