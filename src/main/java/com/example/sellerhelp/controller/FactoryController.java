package com.example.sellerhelp.controller;

import com.cloudinary.api.exceptions.AlreadyExists;
import com.example.sellerhelp.appuser.dto.PageableDto;
import com.example.sellerhelp.appuser.dto.UserDto;
import com.example.sellerhelp.appuser.service.UserService;
import com.example.sellerhelp.common.ApiResponseDto;
import com.example.sellerhelp.factory.dto.*;
import com.example.sellerhelp.factory.service.FactoryService;
import com.example.sellerhelp.tool.entity.Tool;
import com.example.sellerhelp.tool.service.ToolService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/factories")
@RequiredArgsConstructor
public class FactoryController {

    private final FactoryService factoryService;
    private final UserService userService;
    private final ToolService toolService;

    /**
     * Creates a new factory. Only accessible by an ADMIN.
     * @param dto The request body containing factory details.
     * @return The created factory's details.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto<FactoryDetailsDto>> createFactory(@Valid @RequestBody CreateFactoryDto dto) throws AlreadyExists {
        FactoryDetailsDto createdFactory = factoryService.createFactory(dto);
        return new ResponseEntity<>(ApiResponseDto.ok(createdFactory, "Factory created successfully."), HttpStatus.CREATED);
    }

    /**
     * Retrieves a paginated and filterable list of all factories. Only accessible by an ADMIN.
     * @param filter DTO containing filter criteria (name, city, plantHeadName).
     * @param pageableDto DTO for pagination (page, size, sortBy, sortDir).
     * @return A page of factory details.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto<Page<FactoryDetailsDto>>> getAllFactories(
            @ModelAttribute FactoryFilterDto filter,
            @ModelAttribute PageableDto pageableDto) {
        Page<FactoryDetailsDto> factories = factoryService.getAllFactories(filter, pageableDto);
        return ResponseEntity.ok(ApiResponseDto.ok(factories));
    }

    /**
     * Retrieves the details of a single factory by its public ID. Only accessible by an ADMIN.
     * @param factoryId The public ID of the factory.
     * @return The detailed information for the factory.
     */
    @GetMapping("/{factoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto<FactoryDetailsDto>> getFactoryById(@PathVariable String factoryId) {
        FactoryDetailsDto factory = factoryService.getFactoryById(factoryId);
        return ResponseEntity.ok(ApiResponseDto.ok(factory));
    }

    /**
     * Retrieves tool and product stock details for a specific factory.
     * @param factoryId The public ID of the factory.
     * @return A DTO with tool and product stock information.
     */
    @GetMapping("/{factoryId}/inventory") // Using a more descriptive path
    @PreAuthorize("hasAnyRole('ADMIN', 'PLANT_HEAD')") // Plant Head also needs this view
    public ResponseEntity<ApiResponseDto<FactoryToolDetailsDto>> getFactoryToolDetails(@PathVariable String factoryId) {
        FactoryToolDetailsDto factoryInventory = factoryService.getFactoryToolDetails(factoryId);
        return ResponseEntity.ok(ApiResponseDto.ok(factoryInventory));
    }


    /**
     * Updates an existing factory's details. Only accessible by an ADMIN.
     * @param factoryId The public ID of the factory to update.
     * @param dto The request body with the fields to update.
     * @return The updated factory's details.
     */
    @PutMapping("/{factoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto<FactoryDetailsDto>> updateFactory(
            @PathVariable String factoryId,
            @Valid @RequestBody UpdateFactoryDto dto) {
        FactoryDetailsDto updatedFactory = factoryService.updateFactory(factoryId, dto);
        return ResponseEntity.ok(ApiResponseDto.ok(updatedFactory, "Factory updated successfully."));
    }
    /**
     * Deactivates a factory (soft delete). Only accessible by an ADMIN.
     * @param factoryId The public ID of the factory to deactivate.
     * @return A success message.
     */
    @DeleteMapping("/{factoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto<Void>> deactivateFactory(@PathVariable String factoryId) {
        factoryService.deactivateFactory(factoryId);
        return ResponseEntity.ok(ApiResponseDto.ok(null, "Factory deactivated successfully."));
    }

    /**
     * Retrieves all employees for a specific factory.
     * Accessible by ADMIN and PLANT_HEAD.
     * @param factoryId The public ID of the factory.
     * @param pageableDto Pagination information.
     * @return A page of user details.
     */
    @GetMapping("/{factoryId}/employees")
    @PreAuthorize("hasAnyRole('ADMIN', 'PLANT_HEAD')")
    public ResponseEntity<ApiResponseDto<Page<UserDto>>> getEmployeesByFactory(
            @PathVariable String factoryId,
            @ModelAttribute PageableDto pageableDto) {
        // We call the existing, proven method from UserService
        Page<UserDto> employees = userService.getFactoryWorkers(factoryId, pageableDto);
        return ResponseEntity.ok(ApiResponseDto.ok(employees));
    }
//
//    /**
//     * Retrieves all tools for a specific factory.
//     * Accessible by ADMIN and PLANT_HEAD.
//     */
//    @GetMapping("/{factoryId}/tools")
//    @PreAuthorize("hasAnyRole('ADMIN', 'PLANT_HEAD')")
//    public ResponseEntity<ApiResponseDto<Page<?>>> getToolsByFactory(
//            @PathVariable String factoryId,
//            @ModelAttribute PageableDto pageableDto) {
//        // The return type Page<?> is temporary until we create a ToolDto
//        Page<?> tools = toolService.getToolsByFactory(factoryId, pageableDto);
//        return ResponseEntity.ok(ApiResponseDto.ok(tools));
//    }
}
