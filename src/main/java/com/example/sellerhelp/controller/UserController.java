package com.example.sellerhelp.controller;

import com.example.sellerhelp.appuser.dto.*;
import com.example.sellerhelp.appuser.service.UserService;
import com.example.sellerhelp.common.ApiResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // --- CUD OPERATIONS ---

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PLANT_HEAD')")
    public ResponseEntity<ApiResponseDto<UserDto>> createUser(@Valid @RequestBody CreateUserRequestDto requestDto) {
        UserDto createdUser;
        try{
            createdUser = userService.createUser(requestDto);
        } catch (BadRequestException e){
            return ResponseEntity.badRequest()
                    .body(ApiResponseDto.error(e.getMessage(), HttpStatus.BAD_REQUEST));
        }
        return new ResponseEntity<>(ApiResponseDto.ok(createdUser, "User created successfully"), HttpStatus.CREATED);
    }

    @PutMapping("/update/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto<UserDto>> updateUser(
            @PathVariable String userId,
            @Valid @RequestBody UpdateUserRequestDto requestDto) {
        UserDto updatedUser = userService.updateUser(userId, requestDto);
        return ResponseEntity.ok(ApiResponseDto.ok(updatedUser, "User updated successfully"));
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto<Void>> deactivateUser(@PathVariable String userId) {
        userService.deactivateUser(userId);
        return ResponseEntity.ok(ApiResponseDto.ok(null, "User deactivated successfully"));
    }


    @GetMapping("/employees") //
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto<Page<UserDto>>> searchEmployees(
            @ModelAttribute UserFilterDto filter, @RequestParam(value = "q", required = false) String query,
            @ModelAttribute PageableDto pageableDto) {
        Page<UserDto> usersPage = userService.searchEmployees(filter, query, pageableDto);
        return ResponseEntity.ok(ApiResponseDto.ok(usersPage));
    }


    @GetMapping("/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponseDto<UserDto>> getUserById(@PathVariable String userId) {
        UserDto user = userService.getUserById(userId);
        return ResponseEntity.ok(ApiResponseDto.ok(user));
    }


    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto<Page<UserDto>>> globalSearch(
            @RequestParam("q") String query,
            @ModelAttribute PageableDto pageableDto) {
        Page<UserDto> usersPage = userService.globalSearch(query, pageableDto);
        return ResponseEntity.ok(ApiResponseDto.ok(usersPage));
    }



    @GetMapping("/factory/{factoryId}/workers")
    @PreAuthorize("hasAnyRole('ADMIN', 'PLANT_HEAD')")
    public ResponseEntity<ApiResponseDto<Page<UserDto>>> getFactoryWorkers(
            @PathVariable String factoryId,
            @ModelAttribute PageableDto pageableDto) {
        Page<UserDto> usersPage = userService.getFactoryWorkers(factoryId, pageableDto);
        return ResponseEntity.ok(ApiResponseDto.ok(usersPage));
    }

    @GetMapping("/bay/{bayId}/workers")
    @PreAuthorize("hasAnyRole('ADMIN', 'PLANT_HEAD', 'CHIEF_SUPERVISOR')")
    public ResponseEntity<ApiResponseDto<Page<UserDto>>> getBayWorkers(
            @PathVariable String bayId,
            @ModelAttribute PageableDto pageableDto) {
        Page<UserDto> usersPage = userService.getBayWorkers(bayId, pageableDto);
        return ResponseEntity.ok(ApiResponseDto.ok(usersPage));
    }

    @GetMapping("/dealers")
    @PreAuthorize("hasAnyRole('ADMIN', 'CENTRAL_OFFICE')")
    public ResponseEntity<ApiResponseDto<Page<UserDto>>> getDealers(
            @ModelAttribute PageableDto pageableDto) {
        Page<UserDto> usersPage = userService.getAllDealers(pageableDto);
        return ResponseEntity.ok(ApiResponseDto.ok(usersPage));
    }

    @GetMapping("/dashboard/counts")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponseDto<DashboardCountsDto>> getDashboardCounts() {
        DashboardCountsDto data = userService.getCounts();
        return ResponseEntity.ok(ApiResponseDto.ok(data, "Dashboard counts fetched successfully"));
    }
    @PostMapping("/{userId}/upload-image")
    public ResponseEntity<ApiResponseDto<String>> uploadImage(
            @PathVariable String userId,
            @RequestParam("file") MultipartFile file
    ) {
        String imageUrl = userService.uploadUserImage(userId, file);
        return ResponseEntity.ok(ApiResponseDto.ok(imageUrl, "Image uploaded successfully"));
    }
}