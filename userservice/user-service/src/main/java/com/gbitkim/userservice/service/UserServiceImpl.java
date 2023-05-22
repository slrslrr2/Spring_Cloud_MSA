package com.gbitkim.userservice.service;

import com.gbitkim.userservice.dto.UserDto;
import com.gbitkim.userservice.jpa.UserEntity;
import com.gbitkim.userservice.jpa.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public UserDto createUser(UserDto userDto) {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        UserEntity userEntity = modelMapper.map(userDto, UserEntity.class);

        userEntity.setUserId(UUID.randomUUID().toString());
        userEntity.setEncryptedPwd(passwordEncoder.encode(userDto.getPwd()));
        userEntity.setCreateAt(Timestamp.valueOf(LocalDateTime.now()));

        userRepository.save(userEntity);

        return modelMapper.map(userEntity, UserDto.class);
    }
}
