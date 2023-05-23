package com.gbitkim.userservice.service;

import com.gbitkim.userservice.dto.UserDto;

import java.util.List;

public interface UserService {
    UserDto createUser(UserDto userDto);

    UserDto getUserByUserId(String userId);

    List<UserDto> getUserByAll();
}
