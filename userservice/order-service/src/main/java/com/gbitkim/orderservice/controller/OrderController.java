package com.gbitkim.orderservice.controller;

import com.gbitkim.orderservice.dto.OrderDto;
import com.gbitkim.orderservice.jpa.OrderEntity;
import com.gbitkim.orderservice.service.OrderService;
import com.gbitkim.orderservice.vo.RequestOrder;
import com.gbitkim.orderservice.vo.ResponseOrder;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private final Environment env;

    @GetMapping("/health_check")
    public String status(){
        return String.format("It's Working in User Service"
                + ", \nport(local.server.port)=" + env.getProperty("local.server.port")
                + ", \nport(server.port)=" + env.getProperty("server.port")
        );
    }

    @PostMapping("/{userId}/orders")
    public ResponseEntity<?> createOrder(@RequestBody RequestOrder requestOrder,
                                         @PathVariable String userId){
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        OrderDto orderDto = modelMapper.map(requestOrder, OrderDto.class);
        orderDto.setUserId(userId);

        orderDto = orderService.createOrder(orderDto);
        ResponseOrder userResponse = modelMapper.map(orderDto, ResponseOrder.class);
        return new ResponseEntity<>(userResponse, HttpStatus.CREATED);
    }

    @GetMapping("/{userId}/orders")
    public ResponseEntity<?> getOrder(@PathVariable String userId) {
        Iterable<OrderEntity> orders = orderService.getOrderByUserId(userId);

        List<ResponseOrder> results = new ArrayList<>();

        orders.forEach( v -> {
            results.add(new ModelMapper().map(v, ResponseOrder.class));
        });

        return new ResponseEntity<>(results, HttpStatus.OK);
    }

}
