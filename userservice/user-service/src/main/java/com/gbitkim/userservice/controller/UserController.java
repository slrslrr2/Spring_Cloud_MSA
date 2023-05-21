package com.gbitkim.userservice.controller;

import com.gbitkim.userservice.vo.Greeting;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class UserController {
    private final Greeting greeting;

    @GetMapping("/health_check")
    public String status(){
        return "It's Working in User Service";
    }

    @GetMapping("/greeting")
    public String getGreeting(){
        return greeting.getMessage();
    }
}
