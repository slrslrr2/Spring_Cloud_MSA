package com.gbitkim.userservice.dto;

import lombok.Data;

import java.util.Date;

@Data
public class UserDto {
    private String email;
    private String name;
    private String userId;
    private String pwd;
    private String encryptedPwd;
    private Date createAt;
}
