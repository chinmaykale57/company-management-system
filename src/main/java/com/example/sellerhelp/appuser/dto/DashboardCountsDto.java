package com.example.sellerhelp.appuser.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class DashboardCountsDto {
    private long totalActive;
    private long totalWorkers;
    private long totalSupervisors;
    private long totalDealers;
}