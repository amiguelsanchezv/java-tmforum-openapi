package com.tmforum.openapi.controller;

import com.tmforum.openapi.config.SecurityConstants;
import com.tmforum.openapi.dto.CustomerPatchRequest;
import com.tmforum.openapi.dto.CustomerRequest;
import com.tmforum.openapi.dto.CustomerResponse;
import com.tmforum.openapi.dto.PageResponse;
import com.tmforum.openapi.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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

import java.util.Optional;

/**
 * Customer Controller following TMF629 Customer Management API specification
 * Base path: /api/customer (TMF629 standard)
 */
@RestController
@RequestMapping("/customer")
@CrossOrigin(origins = "*")
@Tag(name = "Customer", description = "TMF629 Customer Management API - Operations for Customer Resource")
@SecurityRequirement(name = "bearerAuth")
public class CustomerController {
    
    @Autowired
    private CustomerService customerService;
    
    @Operation(
        summary = "List or find Customer objects",
        description = "List or find Customer objects according to TMF629 specification. " +
                     "Uses offset/limit pagination (TMF629 standard) instead of page/size. " +
                     "Requires scope: customers:read",
        operationId = "listCustomer"
    )
    @ApiResponse(responseCode = "200", description = "Paginated list of customers retrieved successfully")
    @PreAuthorize("hasAuthority('" + SecurityConstants.SCOPE_CUSTOMERS_READ + "') or hasAuthority('" + SecurityConstants.SCOPE_CUSTOMERS_ADMIN + "')")
    @GetMapping
    public ResponseEntity<PageResponse<CustomerResponse>> listCustomer(
            @Parameter(description = "Comma-separated list of fields to include in response")
            @RequestParam(required = false) String fields,
            @Parameter(description = "Requested index for start of resources to be provided in response (TMF629 offset)", example = "0")
            @RequestParam(defaultValue = "0") int offset,
            @Parameter(description = "Requested number of resources to be provided in response (TMF629 limit)", example = "10")
            @RequestParam(defaultValue = "10") int limit,
            @Parameter(description = "Field to sort by", example = "creationDate")
            @RequestParam(defaultValue = "creationDate") String sort,
            @Parameter(description = "Sort direction (ASC or DESC)", example = "DESC")
            @RequestParam(defaultValue = "DESC") String direction) {
        
        // Convert offset/limit to page/size for Spring Data
        int page = offset / limit;
        int size = limit;
        
        Sort.Direction sortDirection = direction.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        
        PageResponse<CustomerResponse> response = customerService.getAllCustomers(pageable);
        return ResponseEntity.ok(response);
    }
    
    @Operation(
        summary = "Retrieves a Customer by ID",
        description = "This operation retrieves a Customer entity. Attribute selection enabled for all first level attributes. " +
                     "Requires scope: customers:read",
        operationId = "retrieveCustomer"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Customer found", 
                    content = @Content(schema = @Schema(implementation = CustomerResponse.class))),
        @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @PreAuthorize("hasAuthority('" + SecurityConstants.SCOPE_CUSTOMERS_READ + "') or hasAuthority('" + SecurityConstants.SCOPE_CUSTOMERS_ADMIN + "')")
    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> retrieveCustomer(
            @Parameter(description = "Identifier of the Customer", required = true)
            @PathVariable String id,
            @Parameter(description = "Comma-separated list of fields to include in response")
            @RequestParam(required = false) String fields) {
        Optional<CustomerResponse> customer = customerService.getCustomerById(id);
        return customer.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
    }
    
    @Operation(
        summary = "Creates a Customer",
        description = "This operation creates a Customer entity according to TMF629 specification. " +
                     "Requires scope: customers:write",
        operationId = "createCustomer"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Customer created successfully",
                    content = @Content(schema = @Schema(implementation = CustomerResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid data or customer already exists"),
        @ApiResponse(responseCode = "409", description = "Conflict - customer already exists")
    })
    @PreAuthorize("hasAuthority('" + SecurityConstants.SCOPE_CUSTOMERS_WRITE + "') or hasAuthority('" + SecurityConstants.SCOPE_CUSTOMERS_ADMIN + "')")
    @PostMapping
    public ResponseEntity<?> createCustomer(
            @Parameter(description = "The Customer to be created (Customer_FVO)", required = true)
            @Valid @RequestBody CustomerRequest request) {
        try {
            CustomerResponse newCustomer = customerService.createCustomer(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(newCustomer);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("already exists")) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("{\"message\": \"" + e.getMessage() + "\"}");
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }
    
    @Operation(
        summary = "Updates partially a Customer",
        description = "This operation updates partially a Customer entity according to TMF629 specification. " +
                     "Requires scope: customers:write",
        operationId = "patchCustomer"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Customer updated successfully",
                    content = @Content(schema = @Schema(implementation = CustomerResponse.class))),
        @ApiResponse(responseCode = "404", description = "Customer not found"),
        @ApiResponse(responseCode = "400", description = "Invalid data"),
        @ApiResponse(responseCode = "409", description = "Conflict - email already exists")
    })
    @PreAuthorize("hasAuthority('" + SecurityConstants.SCOPE_CUSTOMERS_WRITE + "') or hasAuthority('" + SecurityConstants.SCOPE_CUSTOMERS_ADMIN + "')")
    @PatchMapping("/{id}")
    public ResponseEntity<?> patchCustomer(
            @Parameter(description = "Identifier of the Customer", required = true)
            @PathVariable String id,
            @Parameter(description = "The Customer to be patched (Customer_MVO)", required = true)
            @Valid @RequestBody CustomerPatchRequest request) {
        try {
            CustomerResponse updatedCustomer = customerService.patchCustomer(id, request);
            return ResponseEntity.ok(updatedCustomer);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("{\"message\": \"" + e.getMessage() + "\"}");
            }
            if (e.getMessage().contains("already exists")) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("{\"message\": \"" + e.getMessage() + "\"}");
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }
    
    @Operation(
        summary = "Deletes a Customer",
        description = "This operation deletes a Customer entity according to TMF629 specification. " +
                     "Requires scope: customers:delete",
        operationId = "deleteCustomer"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "202", description = "Accepted - deletion in progress"),
        @ApiResponse(responseCode = "204", description = "Customer deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @PreAuthorize("hasAuthority('" + SecurityConstants.SCOPE_CUSTOMERS_DELETE + "') or hasAuthority('" + SecurityConstants.SCOPE_CUSTOMERS_ADMIN + "')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCustomer(
            @Parameter(description = "Identifier of the Customer", required = true)
            @PathVariable String id) {
        try {
            customerService.deleteCustomer(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }
}
