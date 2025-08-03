package org.deepseek.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import org.deepseek.service.ChatMemoryService;
import org.springframework.stereotype.Service;

@Service
@DS("mysql")
public class ChatMemoryServiceImpl implements ChatMemoryService {
}
