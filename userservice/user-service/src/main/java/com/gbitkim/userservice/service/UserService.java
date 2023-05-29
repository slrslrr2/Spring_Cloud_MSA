package com.gbitkim.userservice.service;

import com.gbitkim.userservice.dto.UserDto;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface UserService extends UserDetailsService {
    UserDto createUser(UserDto userDto);

    UserDto getUserByUserId(String userId);

    List<UserDto> getUserByAll();

    UserDto getUserDetailByEmail(String username);
}
