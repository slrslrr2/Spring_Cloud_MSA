package com.gbitkim.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Field {
    private String type; //TODO : ENUM으로 빼놓기
    private boolean optional;
    private String field;
}
