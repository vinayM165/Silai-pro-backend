package com.silaipro.dto.messaging;

import com.silaipro.enums.MessageChannel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class MessageSentResponse {
    private Long id;
    private String customerName;
    private String templateName;
    private String resolvedContent;
    private MessageChannel channel;
    private LocalDateTime sentAt;
    private String sentByName;
}
