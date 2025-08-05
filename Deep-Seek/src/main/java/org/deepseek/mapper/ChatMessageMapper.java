package org.deepseek.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.deepseek.entity.ChatMessage;


@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {
}
