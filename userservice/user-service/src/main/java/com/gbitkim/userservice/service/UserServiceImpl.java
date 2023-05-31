package com.gbitkim.userservice.service;

import com.gbitkim.userservice.dto.UserDto;
import com.gbitkim.userservice.jpa.UserEntity;
import com.gbitkim.userservice.jpa.UserRepository;
import com.gbitkim.userservice.vo.ResponseOrder;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final RestTemplate restTemplate;
    private final Environment env;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity userEntity = userRepository.findByEmail(username);

        if(userEntity == null)
            throw new UsernameNotFoundException(username);

        return new User(userEntity.getEmail(), userEntity.getEncryptedPwd(),
                true, true, true, true,
                new ArrayList<>());
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        UserEntity userEntity = modelMapper.map(userDto, UserEntity.class);

        userEntity.setUserId(UUID.randomUUID().toString());
        userEntity.setEncryptedPwd(passwordEncoder.encode(userDto.getPwd()));
        userEntity.setCreateAt(LocalDateTime.now());

        userRepository.save(userEntity);

        return modelMapper.map(userEntity, UserDto.class);
    }

    @Override
    public List<UserDto> getUserByAll() {
        Iterable<UserEntity> users = userRepository.findAll();
        List<UserDto> returnUsers = new ArrayList<>();
        users.forEach(
            e -> returnUsers.add(new ModelMapper().map(e, UserDto.class)
        ));

        return returnUsers;
    }

    @Override
    public UserDto getUserByUserId(String userId) {
        UserEntity userEntity = userRepository.findByUserId(userId);
        if(userEntity == null)
            throw new UsernameNotFoundException("User not found");

        UserDto userDto = new ModelMapper().map(userEntity, UserDto.class);
        String orderUrl = String.format(env.getProperty("order_service.url"), userId);
        ResponseEntity<List<ResponseOrder>> orderResponses = restTemplate.exchange(orderUrl, HttpMethod.GET
                , null, new ParameterizedTypeReference<List<ResponseOrder>>() {});

        List<ResponseOrder> orderList = orderResponses.getBody();
        userDto.setOrders(orderList);

        return userDto;
    }

    @Override
    public UserDto getUserDetailByEmail(String email) {
        UserEntity userEntity = userRepository.findByEmail(email);

        if(userEntity == null)
            throw new UsernameNotFoundException("User not found");
        
        return new ModelMapper().map(userEntity, UserDto.class);
    }
}
