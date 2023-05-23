package com.gbitkim.catalogservice.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class CatalogDto implements Serializable {
    private String productId;
//    private String productName;
//    private Integer stock;
    private Integer qty;
    private Integer unitPrice;
    private Integer totalPrice;
//    private LocalDateTime createAt;
    private String orderId;
    private String userId;
}
