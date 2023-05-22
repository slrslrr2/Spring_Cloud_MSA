package com.gbitkim.userservice.controller;

import com.gbitkim.userservice.dto.UserDto;
import com.gbitkim.userservice.service.UserService;
import com.gbitkim.userservice.vo.Greeting;
import com.gbitkim.userservice.vo.UserRequest;
import com.gbitkim.userservice.vo.UserResponse;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class UserController {
    private final Greeting greeting;
    private final UserService userService;

    @GetMapping("/health_check")
    public String status(){
        return "It's Working in User Service";
    }

    @GetMapping("/greeting")
    public String getGreeting(){
        return greeting.getMessage();
    }

    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody UserRequest userRequest){
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        UserDto userDto = modelMapper.map(userRequest, UserDto.class);
        userDto = userService.createUser(userDto);
        UserResponse userResponse = modelMapper.map(userDto, UserResponse.class);
        return new ResponseEntity<>(userResponse, HttpStatus.CREATED);
    }
}
