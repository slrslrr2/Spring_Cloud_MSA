package com.gbitkim.userservice.client;

import com.gbitkim.userservice.vo.ResponseOrder;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name="order-service") // 서비스명
public interface OrderServiceClient {

    @GetMapping("/{userId}/orders_ng")
    List<ResponseOrder> getOrders(@PathVariable String userId);
}
