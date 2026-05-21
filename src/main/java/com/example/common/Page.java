package com.example.common;

import lombok.Data;

@Data
public class Page {
    private Integer currentPage=1;
    private Integer pageSize=10;
}
