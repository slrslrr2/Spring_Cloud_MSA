package com.gbitkim.orderservice.jpa;


import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Entity
@Table(name="orders")
public class OrderEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, Length = 120, unique = true)
    private String productId

    @Column (nullable = false)
    private Integer qty;
    @Column (nullable = false)
    private Integer unitPrice:
    CoLumn (nullable = false)
    private Integer totalrice;
}