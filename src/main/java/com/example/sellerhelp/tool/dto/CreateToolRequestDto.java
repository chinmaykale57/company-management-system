package com.example.sellerhelp.tool.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateToolRequestDto {

    @NotEmpty(message = "The request must contain at least one tool.")
    @Valid
    private List<ToolRequestItemDto> tools;

    private String comment; //optional
}