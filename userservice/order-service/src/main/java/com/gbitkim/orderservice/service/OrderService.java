package com.gbitkim.orderservice.service;

import com.gbitkim.orderservice.dto.OrderDto;
import com.gbitkim.orderservice.jpa.OrderEntity;

public interface OrderService {
    OrderDto createOrder(OrderDto orderDetails);
    OrderDto getOrderByOrderId(String orderId);
    Iterable<OrderEntity> getOrderByUserId(String userId);
}