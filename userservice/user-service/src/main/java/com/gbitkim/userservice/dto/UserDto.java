package com.gbitkim.userservice.dto;

import com.gbitkim.userservice.vo.ResponseOrder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class UserDto {
    private String email;
    private String name;
    private String userId;
    private String pwd;
    private String encryptedPwd;
    private LocalDateTime createAt;

    List<ResponseOrder> orderList = new ArrayList<>();
}
