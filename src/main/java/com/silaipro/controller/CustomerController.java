package com.silaipro.controller;

import com.silaipro.dto.customer.CustomerFilter;
import com.silaipro.dto.customer.CustomerHistoryResponse;
import com.silaipro.dto.customer.CustomerRequest;
import com.silaipro.dto.customer.CustomerResponse;
import com.silaipro.security.HasPermission;
import com.silaipro.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Tag(name = "Customers", description = "Customer management endpoints")
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping
    @HasPermission("VIEW_CUSTOMER")
    @Operation(summary = "Get all customers with pagination and filtering")
    public Page<CustomerResponse> getAllCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @ModelAttribute CustomerFilter filter) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return customerService.searchCustomers(filter, pageable);
    }

    @PostMapping
    @HasPermission("CREATE_CUSTOMER")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new customer")
    @ApiResponse(responseCode = "201", description = "Customer created successfully")
    public CustomerResponse createCustomer(@Valid @RequestBody CustomerRequest request) {
        return customerService.createCustomer(request);
    }

    @GetMapping("/{id}")
    @HasPermission("VIEW_CUSTOMER")
    @Operation(summary = "Get customer by ID")
    @ApiResponse(responseCode = "200", description = "Customer found")
    @ApiResponse(responseCode = "404", description = "Customer not found")
    public CustomerResponse getCustomer(@PathVariable Long id) {
        return customerService.getCustomerById(id);
    }

    @PutMapping("/{id}")
    @HasPermission("UPDATE_CUSTOMER")
    @Operation(summary = "Update customer details")
    public CustomerResponse updateCustomer(@PathVariable Long id, @Valid @RequestBody CustomerRequest request) {
        return customerService.updateCustomer(id, request);
    }

    @DeleteMapping("/{id}")
    @HasPermission("DELETE_CUSTOMER")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a customer")
    public void deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
    }

    @GetMapping("/{id}/history")
    @HasPermission("VIEW_CUSTOMER")
    @Operation(summary = "Get customer order and payment history")
    public CustomerHistoryResponse getCustomerHistory(@PathVariable Long id) {
        return customerService.getCustomerHistory(id);
    }
}
