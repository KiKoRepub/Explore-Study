package org.dee.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDTO  {
    private String userMessage;
    private String botResponse;
}
