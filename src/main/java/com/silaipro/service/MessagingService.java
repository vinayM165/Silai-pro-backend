package com.silaipro.service;

import com.silaipro.dto.messaging.*;
import com.silaipro.entity.*;
import com.silaipro.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessagingService {

    private final MessageTemplateRepository templateRepository;
    private final MessageSentRepository messageSentRepository;
    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;
    private final InvoiceRepository invoiceRepository;
    private final ShopSettingRepository shopSettingRepository;
    private final UserRepository userRepository;
    private final InvoiceService invoiceService;

    @Transactional
    public TemplateResponse createTemplate(TemplateRequest request) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User creator = userRepository.findByPhoneOrEmail(currentUsername)
                .orElse(null);

        MessageTemplate template = MessageTemplate.builder()
                .name(request.getName())
                .content(request.getContent())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .createdBy(creator)
                .build();

        template = templateRepository.save(template);
        return mapToTemplateResponse(template);
    }

    @Transactional
    public TemplateResponse updateTemplate(Long id, TemplateRequest request) {
        MessageTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Template not found"));

        template.setName(request.getName());
        template.setContent(request.getContent());
        if (request.getIsActive() != null) {
            template.setIsActive(request.getIsActive());
        }

        template = templateRepository.save(template);
        return mapToTemplateResponse(template);
    }

    @Transactional
    public void deleteTemplate(Long id) {
        templateRepository.deleteById(id);
    }

    public List<TemplateResponse> getAllTemplates() {
        return templateRepository.findAll().stream()
                .map(this::mapToTemplateResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public MessageSentResponse sendMessage(SendMessageRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));

        String content;
        MessageTemplate template = null;

        if (request.getTemplateId() != null) {
            template = templateRepository.findById(request.getTemplateId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Template not found"));
            content = template.getContent();
        } else {
            content = request.getCustomContent();
        }

        if (content == null || content.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Message content cannot be empty");
        }

        String resolvedContent = resolveVariables(content, customer, request.getOrderId());

        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User sender = userRepository.findByPhoneOrEmail(currentUsername).orElse(null);

        MessageSent messageSent = MessageSent.builder()
                .customer(customer)
                .template(template)
                .content(resolvedContent)
                .channel(request.getChannel())
                .sentBy(sender)
                .build();

        messageSent = messageSentRepository.save(messageSent);
        return mapToMessageSentResponse(messageSent);
    }

    public List<MessageSentResponse> getMessageHistory(Long customerId) {
        return messageSentRepository.findByCustomerId(customerId).stream()
                .map(this::mapToMessageSentResponse)
                .collect(Collectors.toList());
    }

    private String resolveVariables(String content, Customer customer, Long orderId) {
        String resolved = content;
        resolved = resolved.replace("{customer_name}", customer.getName());
        
        String shopName = shopSettingRepository.findBySettingKey("SHOP_NAME")
                .map(ShopSetting::getSettingValue)
                .orElse("Silai Pro");
        resolved = resolved.replace("{shop_name}", shopName);

        if (orderId != null) {
            Order order = orderRepository.findById(orderId).orElse(null);
            if (order != null) {
                resolved = resolved.replace("{order_id}", order.getOrderNo());
                resolved = resolved.replace("{amount}", order.getTotalAmount().toString());
                
                try {
                    BigDecimal balance = invoiceService.getInvoiceByOrder(orderId).getBalanceDue();
                    resolved = resolved.replace("{balance}", balance.toString());
                } catch (Exception e) {
                    resolved = resolved.replace("{balance}", "0.00");
                }
            }
        }

        return resolved;
    }

    private TemplateResponse mapToTemplateResponse(MessageTemplate t) {
        return TemplateResponse.builder()
                .id(t.getId())
                .name(t.getName())
                .content(t.getContent())
                .isActive(t.getIsActive())
                .createdAt(t.getCreatedAt())
                .build();
    }

    private MessageSentResponse mapToMessageSentResponse(MessageSent m) {
        return MessageSentResponse.builder()
                .id(m.getId())
                .customerName(m.getCustomer().getName())
                .templateName(m.getTemplate() != null ? m.getTemplate().getName() : "Custom")
                .resolvedContent(m.getContent())
                .channel(m.getChannel())
                .sentAt(m.getSentAt())
                .sentByName(m.getSentBy() != null ? m.getSentBy().getName() : "System")
                .build();
    }
}
