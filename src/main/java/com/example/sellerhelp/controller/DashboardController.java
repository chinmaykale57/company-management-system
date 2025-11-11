package com.example.sellerhelp.controller;

import com.example.sellerhelp.common.ApiResponseDto;
import com.example.sellerhelp.dashboard.dto.CentralOfficeDashboardDto;
import com.example.sellerhelp.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard/central-office")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * Retrieves all aggregated data for the Central Office dashboard.
     * This includes card metrics, sales overviews, and product insights.
     * Accessible only by users with the CENTRAL_OFFICE or ADMIN role.
     * @return A DTO containing all dashboard metrics.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CENTRAL_OFFICE')")
    public ResponseEntity<ApiResponseDto<CentralOfficeDashboardDto>> getCentralOfficeDashboard() {
        CentralOfficeDashboardDto dashboardData = dashboardService.getDashboardData();
        return ResponseEntity.ok(ApiResponseDto.ok(dashboardData, "Dashboard data fetched successfully."));
    }
}