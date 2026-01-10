package com.tmforum.openapi.controller.v1;

import com.tmforum.openapi.config.SecurityConstants;
import com.tmforum.openapi.dto.CustomerRequestV1;
import com.tmforum.openapi.dto.CustomerResponseV1;
import com.tmforum.openapi.dto.PageResponse;
import com.tmforum.openapi.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller v1 for customer management
 */
@RestController
@RequestMapping("/v1/customers")
@Tag(name = "Customers v1", description = "API v1 for customer management")
@SecurityRequirement(name = "bearerAuth")
public class CustomerV1Controller {
    
    @Autowired
    private CustomerService customerService;
    
    @GetMapping
    @Operation(summary = "Get all customers (v1)", description = "Returns a paginated list of all customers")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of customers retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasAuthority('" + SecurityConstants.SCOPE_CUSTOMERS_READ + "') or hasAuthority('" + SecurityConstants.SCOPE_CUSTOMERS_ADMIN + "')")
    public ResponseEntity<PageResponse<CustomerResponseV1>> getAllCustomers(
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Field to sort by", example = "creationDate")
            @RequestParam(defaultValue = "creationDate") String sort,
            @Parameter(description = "Sort direction (ASC or DESC)", example = "DESC")
            @RequestParam(defaultValue = "DESC") String direction) {
        
        Sort.Direction sortDirection = direction.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        
        PageResponse<CustomerResponseV1> response = customerService.getAllCustomersV1(pageable);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get customer by ID (v1)", description = "Returns a specific customer by their ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Customer found"),
        @ApiResponse(responseCode = "404", description = "Customer not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasAuthority('" + SecurityConstants.SCOPE_CUSTOMERS_READ + "') or hasAuthority('" + SecurityConstants.SCOPE_CUSTOMERS_ADMIN + "')")
    public ResponseEntity<CustomerResponseV1> getCustomerById(
            @Parameter(description = "Customer ID")
            @PathVariable String id) {
        return customerService.getCustomerByIdV1(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    @Operation(summary = "Create new customer (v1)", description = "Creates a new customer in the system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Customer created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasAuthority('" + SecurityConstants.SCOPE_CUSTOMERS_WRITE + "') or hasAuthority('" + SecurityConstants.SCOPE_CUSTOMERS_ADMIN + "')")
    public ResponseEntity<CustomerResponseV1> createCustomer(@Valid @RequestBody CustomerRequestV1 request) {
        CustomerResponseV1 response = customerService.createCustomerV1(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update customer (v1)", description = "Updates an existing customer")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Customer updated successfully"),
        @ApiResponse(responseCode = "404", description = "Customer not found"),
        @ApiResponse(responseCode = "400", description = "Invalid data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasAuthority('" + SecurityConstants.SCOPE_CUSTOMERS_WRITE + "') or hasAuthority('" + SecurityConstants.SCOPE_CUSTOMERS_ADMIN + "')")
    public ResponseEntity<CustomerResponseV1> updateCustomer(
            @Parameter(description = "Customer ID")
            @PathVariable String id,
            @Valid @RequestBody CustomerRequestV1 request) {
        try {
            CustomerResponseV1 updatedCustomer = customerService.updateCustomerV1(id, request);
            return ResponseEntity.ok(updatedCustomer);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete customer (v1)", description = "Deletes a customer from the system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Customer deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Customer not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasAuthority('" + SecurityConstants.SCOPE_CUSTOMERS_DELETE + "') or hasAuthority('" + SecurityConstants.SCOPE_CUSTOMERS_ADMIN + "')")
    public ResponseEntity<Void> deleteCustomer(
            @Parameter(description = "Customer ID")
            @PathVariable String id) {
        try {
            customerService.deleteCustomer(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/search")
    @Operation(summary = "Search customers (v1)", description = "Searches customers by name or surname")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search completed successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasAuthority('" + SecurityConstants.SCOPE_CUSTOMERS_READ + "') or hasAuthority('" + SecurityConstants.SCOPE_CUSTOMERS_ADMIN + "')")
    public ResponseEntity<PageResponse<CustomerResponseV1>> searchCustomers(
            @Parameter(description = "Text to search in name or surname", required = true)
            @RequestParam String name,
            @Parameter(description = "Page number", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Field to sort by", example = "firstName")
            @RequestParam(defaultValue = "firstName") String sort,
            @Parameter(description = "Sort direction", example = "ASC")
            @RequestParam(defaultValue = "ASC") String direction) {
        
        Sort.Direction sortDirection = direction.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        
        PageResponse<CustomerResponseV1> response = customerService.searchCustomersV1(name, pageable);
        return ResponseEntity.ok(response);
    }
}


