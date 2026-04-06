package com.silaipro.controller;

import com.silaipro.dto.messaging.*;
import com.silaipro.security.HasPermission;
import com.silaipro.service.MessagingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Messages", description = "Messaging and template management")
public class MessagingController {

    private final MessagingService messagingService;

    @GetMapping("/api/message-templates")
    @HasPermission("VIEW_CUSTOMER")
    @Operation(summary = "Get all message templates")
    public List<TemplateResponse> getAllTemplates() {
        return messagingService.getAllTemplates();
    }

    @PostMapping("/api/message-templates")
    @HasPermission("MESSAGE_TEMPLATES_MANAGE")
    @Operation(summary = "Create a new message template")
    public TemplateResponse createTemplate(@Valid @RequestBody TemplateRequest request) {
        return messagingService.createTemplate(request);
    }

    @PutMapping("/api/message-templates/{id}")
    @HasPermission("MESSAGE_TEMPLATES_MANAGE")
    @Operation(summary = "Update an existing message template")
    public TemplateResponse updateTemplate(@PathVariable Long id, @Valid @RequestBody TemplateRequest request) {
        return messagingService.updateTemplate(id, request);
    }

    @DeleteMapping("/api/message-templates/{id}")
    @HasPermission("MESSAGE_TEMPLATES_MANAGE")
    @Operation(summary = "Delete a message template")
    public void deleteTemplate(@PathVariable Long id) {
        messagingService.deleteTemplate(id);
    }

    @PostMapping("/api/messages/send")
    @HasPermission("MESSAGE_SEND")
    @Operation(summary = "Send a message using a template or custom content")
    public MessageSentResponse sendMessage(@Valid @RequestBody SendMessageRequest request) {
        return messagingService.sendMessage(request);
    }

    @GetMapping("/api/customers/{id}/messages")
    @HasPermission("VIEW_CUSTOMER")
    @Operation(summary = "Get message history for a customer")
    public List<MessageSentResponse> getMessageHistory(@PathVariable Long id) {
        return messagingService.getMessageHistory(id);
    }
}
