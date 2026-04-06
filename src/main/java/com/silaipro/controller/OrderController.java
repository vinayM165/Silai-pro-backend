package com.silaipro.controller;

import com.silaipro.dto.order.*;
import com.silaipro.enums.OrderStatus;
import com.silaipro.security.HasPermission;
import com.silaipro.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order management endpoints")
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    @HasPermission("ORDER_VIEW")
    @Operation(summary = "Get all orders with pagination and filtering")
    public Page<OrderResponse> getOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @ModelAttribute OrderFilter filter) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return orderService.getOrders(filter, pageable);
    }

    @PostMapping
    @HasPermission("ORDER_CREATE")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new order")
    public OrderResponse createOrder(@Valid @RequestBody OrderRequest request) {
        return orderService.createOrder(request);
    }

    @GetMapping("/{id}")
    @HasPermission("ORDER_VIEW")
    @Operation(summary = "Get order by ID")
    public OrderResponse getOrderById(@PathVariable Long id) {
        return orderService.getOrderById(id);
    }

    @PutMapping("/{id}")
    @HasPermission("ORDER_UPDATE")
    @Operation(summary = "Update order details")
    public OrderResponse updateOrder(@PathVariable Long id, @Valid @RequestBody OrderRequest request) {
        return orderService.updateOrder(id, request);
    }

    @DeleteMapping("/{id}")
    @HasPermission("ORDER_DELETE")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete an order")
    public void deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
    }

    @PatchMapping("/{id}/status")
    @HasPermission("ORDER_UPDATE")
    @Operation(summary = "Update order status")
    public OrderResponse updateStatus(@PathVariable Long id, @RequestBody Map<String, String> statusMap) {
        String statusStr = statusMap.get("status");
        if (statusStr == null) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.BAD_REQUEST, "Status field is required");
        }
        return orderService.updateOrderStatus(id, OrderStatus.valueOf(statusStr));
    }

    @GetMapping("/dashboard")
    @HasPermission("ORDER_VIEW")
    @Operation(summary = "Get dashboard counts for orders")
    public DashboardCountsResponse getDashboardCounts() {
        return orderService.getDashboardCounts();
    }
}
