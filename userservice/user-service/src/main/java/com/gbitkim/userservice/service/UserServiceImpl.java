package com.gbitkim.userservice.service;

import com.gbitkim.userservice.client.OrderServiceClient;
import com.gbitkim.userservice.dto.UserDto;
import com.gbitkim.userservice.error.FeignErrorDecoder;
import com.gbitkim.userservice.jpa.UserEntity;
import com.gbitkim.userservice.jpa.UserRepository;
import com.gbitkim.userservice.vo.ResponseOrder;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
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
@Slf4j
public class UserServiceImpl implements UserService{
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final RestTemplate restTemplate;
    private final Environment env;
    private final OrderServiceClient orderServiceClient;
    private final FeignErrorDecoder feignErrorDecoder;

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

// FeignErrorDecoder 생성 전 FeignClient Exception 처리
//        List<ResponseOrder> orderList = new ArrayList<>();
//        try {
//            // Using a feign client
//            orderList = orderServiceClient.getOrders(userId);
//        } catch (FeignException e){
//            log.error(e.getMessage());
//        }

        List<ResponseOrder> orderList = orderServiceClient.getOrders(userId);
        userDto.setOrders(orderList);
        return userDto;
    }

    @Nullable
    private List<ResponseOrder> getResponseOrdersByFeignClient(String userId) {
        String orderUrl = String.format(env.getProperty("order_service.url"), userId);
        ResponseEntity<List<ResponseOrder>> orderResponses = restTemplate.exchange(orderUrl, HttpMethod.GET
                , null, new ParameterizedTypeReference<>() {
                });
        List<ResponseOrder> orderList = orderResponses.getBody();
        return orderList;
    }

    @Override
    public UserDto getUserDetailByEmail(String email) {
        UserEntity userEntity = userRepository.findByEmail(email);

        if(userEntity == null)
            throw new UsernameNotFoundException("User not found");
        
        return new ModelMapper().map(userEntity, UserDto.class);
    }
}
