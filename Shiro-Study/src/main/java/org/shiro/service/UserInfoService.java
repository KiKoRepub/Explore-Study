package org.shiro.service;

import org.shiro.dao.UserInfoDao;
import org.shiro.entity.UserInfo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;


public interface UserInfoService {

    UserInfo findByUsername(String username);
}
