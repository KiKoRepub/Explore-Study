package org.dee.service.impl;

import org.dee.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {


    @Override
    public String analyzeUserIdFromToken(String userToken) {
        return userToken;
    }
}
