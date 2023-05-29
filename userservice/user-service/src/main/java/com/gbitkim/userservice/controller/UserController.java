package com.gbitkim.userservice.controller;

import com.gbitkim.userservice.dto.UserDto;
import com.gbitkim.userservice.service.UserService;
import com.gbitkim.userservice.vo.Greeting;
import com.gbitkim.userservice.vo.RequestUser;
import com.gbitkim.userservice.vo.ResponseUser;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class UserController {
    private final Greeting greeting;
    private final UserService userService;
    private final Environment env;

    @GetMapping("/health_check")
    public String status(){
        return String.format("It's Working in User Service"
                + ", \nport(local.server.port)=" + env.getProperty("local.server.port")
                + ", \nport(server.port)=" + env.getProperty("server.port")
                + ", \ntoken secret=" + env.getProperty("token.secret")
                + ", \nptoken expiration time=" + env.getProperty("token.expiration_hours")
        );
    }

    @GetMapping("/greeting")
    public String getGreeting(){
        return greeting.getMessage();
    }

    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody RequestUser requestUser){
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        UserDto userDto = modelMapper.map(requestUser, UserDto.class);
        userDto = userService.createUser(userDto);
        ResponseUser userResponse = modelMapper.map(userDto, ResponseUser.class);
        return new ResponseEntity<>(userResponse, HttpStatus.CREATED);
    }

    @GetMapping("/users")
    public ResponseEntity<?> getUsers(){
        List<UserDto> users = userService.getUserByAll();

        ArrayList<ResponseUser> responseUsers = new ArrayList<>();
        users.forEach(u -> responseUsers.add(new ModelMapper().map(u, ResponseUser.class)));

        return new ResponseEntity<>(responseUsers, HttpStatus.OK);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<?> getUserByUserId(@PathVariable String userId){
        UserDto user = userService.getUserByUserId(userId);
        ResponseUser responseUser = new ModelMapper().map(user, ResponseUser.class);

        return new ResponseEntity<>(responseUser, HttpStatus.OK);
    }
}
