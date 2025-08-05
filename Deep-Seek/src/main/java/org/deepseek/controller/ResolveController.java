package org.deepseek.controller;

import com.alibaba.fastjson.JSON;
import org.deepseek.entity.resolve.Address;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/resolve")
public class ResolveController extends AIController {


    @PostMapping("/address")
    public String resolveAddress(@RequestParam("message") String message) {

        Address entity = chatClient.prompt("请将地址解析成中文")
                .user(message)
                .call()
                .entity(Address.class);

        System.out.println(entity);

        return JSON.toJSONString(entity);
    }

}
