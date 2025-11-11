package com.example.sellerhelp.factory.dto;

import com.example.sellerhelp.tool.entity.Tool;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FactoryToolDetailsDto {
    private String factoryId;
    private String City;
    private String address;
    private List<Tool> tools;
    private ProductStockDto productStock;


}
