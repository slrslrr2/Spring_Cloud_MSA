package com.gbitkim.orderservice.service;

import com.gbitkim.orderservice.dto.OrderDto;
import com.gbitkim.orderservice.jpa.OrderEntity;
import com.gbitkim.orderservice.jpa.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService{
    private final OrderRepository orderRepository;

    @Override
    public OrderDto createOrder(OrderDto orderDetails) {
        orderDetails.setOrderId(UUID.randomUUID().toString());
        orderDetails.setTotalPrice(orderDetails.getUnitPrice() * orderDetails.getQty());

        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        OrderEntity orderEntity = modelMapper.map(orderDetails, OrderEntity.class);

        orderEntity.setCreateAt(LocalDateTime.now());

        orderRepository.save(orderEntity);

        return modelMapper.map(orderEntity, OrderDto.class);
    }

    @Override
    public OrderDto getOrderByOrderId(String orderId) {
        OrderEntity orderEntity = orderRepository.findByOrderId(orderId);
        return new ModelMapper().map(orderEntity, OrderDto.class);
    }

    @Override
    public Iterable<OrderEntity> getOrderByUserId(String userId) {
        return orderRepository.findByUserId(userId);
    }
}
