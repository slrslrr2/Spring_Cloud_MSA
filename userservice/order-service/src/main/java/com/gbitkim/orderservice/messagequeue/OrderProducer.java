package com.gbitkim.orderservice.messagequeue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gbitkim.orderservice.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;

    List<Field> fields = Arrays.asList(
            new Field("string", true, "order_id"),
            new Field("string", true, "user_id"),
            new Field("string", true, "product_id"),
            new Field("int32", true, "qty"),
            new Field("int32", true, "unit_price"),
            new Field("int32", true, "total_price")
    );

    Schema schema = Schema.builder()
            .type("struct")
            .fields(fields)
            .optional(false)
            .name("orders")
            .build();

    public OrderDto send(String topic, OrderDto orderDto) {
        Payload payload = Payload.builder()
                .order_id(orderDto.getOrderId())
                .user_id(orderDto.getUserId())
                .product_id(orderDto.getProductId())
                .qty(orderDto.getQty())
                .unit_price(orderDto.getUnitPrice())
                .total_price(orderDto.getTotalPrice())
                .build();

        KafkaOrderDto kafkaOrderDto = new KafkaOrderDto(schema, payload);

        ObjectMapper mapper = new ObjectMapper();
        String jsonInString = "";
        try{
            jsonInString = mapper.writeValueAsString(kafkaOrderDto);
        } catch (JsonProcessingException ex) {
            ex.printStackTrace();
        }

        jsonInString = "{\"schema\":{\"type\":\"struct\",\"fields\":[{\"type\":\"int64\",\"optional\":false,\"field\":\"id\"},{\"type\":\"int64\",\"optional\":false,\"name\":\"org.apache.kafka.connect.data.Timestamp\",\"version\":1,\"field\":\"create_at\"},{\"type\":\"string\",\"optional\":false,\"field\":\"order_id\"},{\"type\":\"string\",\"optional\":false,\"field\":\"product_id\"},{\"type\":\"int32\",\"optional\":false,\"field\":\"qty\"},{\"type\":\"int32\",\"optional\":false,\"field\":\"total_price\"},{\"type\":\"int32\",\"optional\":false,\"field\":\"unit_price\"},{\"type\":\"string\",\"optional\":false,\"field\":\"user_id\"}],\"optional\":false,\"name\":\"test\"},\"payload\":{\"id\":17,\"create_at\":1686094765000,\"order_id\":\"1daa3cbc-b10b-4a6a-8bbb-3eb7be97cd80\",\"product_id\":\"CATALOG-0002\",\"qty\":1,\"total_price\":1200,\"unit_price\":1200,\"user_id\":\"2bab39e4-e856-4111-a404-012615c341c0\"}}";
        kafkaTemplate.send("test_send_test", jsonInString);
        kafkaTemplate.send("test", jsonInString);
        log.info("Order Producer send data from the Order microservice: %s", kafkaOrderDto);

        return orderDto;
    }
}
