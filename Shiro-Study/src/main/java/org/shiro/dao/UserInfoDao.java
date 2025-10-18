package org.shiro.dao;

import org.shiro.entity.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserInfoDao extends JpaRepository<UserInfo, Long> {

    public UserInfo findByUsername(String username);

}
