package com.silaipro.dto.messaging;

import com.silaipro.enums.MessageChannel;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class SendMessageRequest {
    @NotNull(message = "Customer ID is required")
    private Long customerId;

    private Long templateId;
    private String customContent;

    @NotNull(message = "Message channel is required")
    private MessageChannel channel;
    
    private Long orderId;
}
