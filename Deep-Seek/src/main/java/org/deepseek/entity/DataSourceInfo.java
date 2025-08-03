package org.deepseek.entity;

import lombok.Data;

@Data
public class DataSourceInfo {

   private String url;
   private String userName;
   private String password;
   private String driverClassName;

}
