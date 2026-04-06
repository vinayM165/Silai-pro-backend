package com.silaipro.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.silaipro.dto.order.*;
import com.silaipro.entity.*;
import com.silaipro.enums.OrderStatus;
import com.silaipro.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));

        User assignedTo = null;
        if (request.getAssignedTo() != null) {
            assignedTo = userRepository.findById(request.getAssignedTo())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assigned user not found"));
        }

        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User createdBy = userRepository.findByPhoneOrEmail(currentUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Logged in user not found"));

        String orderNo = generateOrderNo();
        BigDecimal totalAmount = request.getItems().stream()
                .map(item -> item.getPricePerPiece().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = Order.builder()
                .customer(customer)
                .orderNo(orderNo)
                .itemsJson(serializeItems(request.getItems()))
                .totalAmount(totalAmount)
                .advancePaid(request.getAdvancePaid())
                .status(OrderStatus.RECEIVED)
                .deliveryDate(request.getExpectedDelivery())
                .priority(request.getPriority())
                .specialInstructions(request.getSpecialInstructions())
                .trialDate(request.getTrialDate())
                .assignedTo(assignedTo)
                .createdBy(createdBy)
                .build();

        order = orderRepository.save(order);
        return mapToResponse(order);
    }

    @Transactional
    public OrderResponse updateOrder(Long id, OrderRequest request) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        if (request.getCustomerId() != null) {
            Customer customer = customerRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
            order.setCustomer(customer);
        }

        if (request.getAssignedTo() != null) {
            User assignedTo = userRepository.findById(request.getAssignedTo())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assigned user not found"));
            order.setAssignedTo(assignedTo);
        }

        BigDecimal totalAmount = request.getItems().stream()
                .map(item -> item.getPricePerPiece().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setItemsJson(serializeItems(request.getItems()));
        order.setTotalAmount(totalAmount);
        order.setAdvancePaid(request.getAdvancePaid());
        order.setDeliveryDate(request.getExpectedDelivery());
        order.setPriority(request.getPriority());
        order.setSpecialInstructions(request.getSpecialInstructions());
        order.setTrialDate(request.getTrialDate());

        order = orderRepository.save(order);
        return mapToResponse(order);
    }

    @Transactional
    public void deleteOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        orderRepository.delete(order);

        auditLogService.log("DELETE_ORDER", "ORDER", id, order.getOrderNo(), null);
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long id, OrderStatus newStatus) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        validateStatusTransition(order.getStatus(), newStatus);

        order.setStatus(newStatus);
        order = orderRepository.save(order);
        return mapToResponse(order);
    }

    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        return mapToResponse(order);
    }

    public Page<OrderResponse> getOrders(OrderFilter filter, Pageable pageable) {
        OrderStatus status = null;
        if (filter.getStatus() != null) {
            status = OrderStatus.valueOf(filter.getStatus());
        }

        return orderRepository.findAllFiltered(
                status,
                filter.getCustomerId(),
                filter.getSearch(),
                filter.getFrom(),
                filter.getTo(),
                filter.getAssignedTo(),
                pageable).map(this::mapToResponse);
    }

    public DashboardCountsResponse getDashboardCounts() {
        LocalDate today = LocalDate.now();
        Map<OrderStatus, Long> statusCounts = new EnumMap<>(OrderStatus.class);
        for (OrderStatus status : OrderStatus.values()) {
            statusCounts.put(status, orderRepository.countByStatus(status));
        }

        return DashboardCountsResponse.builder()
                .todayDeliveries(orderRepository.findTodayDeliveries(today).size())
                .overdueOrders(orderRepository.findOverdueOrders(today).size())
                .statusCounts(statusCounts)
                .build();
    }

    private String generateOrderNo() {
        String year = String.valueOf(LocalDate.now().getYear());
        String maxOrderNo = orderRepository.findMaxOrderNoByYear(year);
        int seq = 1;

        if (maxOrderNo != null) {
            String seqStr = maxOrderNo.substring(maxOrderNo.lastIndexOf("-") + 1);
            seq = Integer.parseInt(seqStr) + 1;
        }

        return String.format("ORD-%s-%03d", year, seq);
    }

    private void validateStatusTransition(OrderStatus current, OrderStatus next) {
        if (current == next) return;
        if (next == OrderStatus.CANCELLED) return;

        Map<OrderStatus, List<OrderStatus>> allowed = new EnumMap<>(OrderStatus.class);
        allowed.put(OrderStatus.RECEIVED, List.of(OrderStatus.CUTTING));
        allowed.put(OrderStatus.CUTTING, List.of(OrderStatus.STITCHING));
        allowed.put(OrderStatus.STITCHING, List.of(OrderStatus.TRIAL_PENDING, OrderStatus.READY));
        allowed.put(OrderStatus.TRIAL_PENDING, List.of(OrderStatus.ALTERATION, OrderStatus.READY));
        allowed.put(OrderStatus.ALTERATION, List.of(OrderStatus.READY));
        allowed.put(OrderStatus.READY, List.of(OrderStatus.DELIVERED));
        allowed.put(OrderStatus.DELIVERED, Collections.emptyList());
        allowed.put(OrderStatus.CANCELLED, Collections.emptyList());

        List<OrderStatus> possible = allowed.getOrDefault(current, Collections.emptyList());
        if (!possible.contains(next)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Illegal status transition from " + current + " to " + next);
        }
    }

    private String serializeItems(List<OrderItem> items) {
        try {
            return objectMapper.writeValueAsString(items);
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error serializing order items");
        }
    }

    private List<OrderItem> deserializeItems(String json) {
        try {
            return Arrays.asList(objectMapper.readValue(json, OrderItem[].class));
        } catch (JsonProcessingException e) {
            return Collections.emptyList();
        }
    }

    private OrderResponse mapToResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .orderNo(order.getOrderNo())
                .customerId(order.getCustomer().getId())
                .customerName(order.getCustomer().getName())
                .items(deserializeItems(order.getItemsJson()))
                .totalAmount(order.getTotalAmount())
                .advancePaid(order.getAdvancePaid())
                .status(order.getStatus())
                .deliveryDate(order.getDeliveryDate())
                .priority(order.getPriority())
                .specialInstructions(order.getSpecialInstructions())
                .trialDate(order.getTrialDate())
                .assignedTo(order.getAssignedTo() != null ? order.getAssignedTo().getId() : null)
                .assignedToName(order.getAssignedTo() != null ? order.getAssignedTo().getName() : null)
                .createdBy(order.getCreatedBy() != null ? order.getCreatedBy().getId() : null)
                .createdByName(order.getCreatedBy() != null ? order.getCreatedBy().getName() : null)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
