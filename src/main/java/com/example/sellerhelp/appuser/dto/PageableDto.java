package com.example.sellerhelp.appuser.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PageableDto {
    private int page = 0;
    private int size = 10;
    private String sortBy = "user_id";
    private String sortDir = "asc";
}
