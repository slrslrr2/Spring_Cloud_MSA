package com.gbitkim.userservice.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gbitkim.userservice.dto.UserDto;
import com.gbitkim.userservice.service.UserService;
import com.gbitkim.userservice.vo.RequestLogin;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;

@Slf4j
@RequiredArgsConstructor
public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private final UserService userService;
    private final Environment env;

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response) throws AuthenticationException {
        try {
            RequestLogin requestLogin = new ObjectMapper().readValue(request.getInputStream(), RequestLogin.class);

            return getAuthenticationManager().authenticate(
                new UsernamePasswordAuthenticationToken(
                        requestLogin.getEmail(), requestLogin.getPassword(), new ArrayList<>())
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authResult) throws IOException, ServletException {
        // 로그인 성공 시 JWT 토큰 발행
        String username = ((User) authResult.getPrincipal()).getUsername();
        UserDto userDetail = userService.getUserDetailByEmail(username);

        LocalDateTime localDateTime = LocalDateTime.now().plusHours(Long.parseLong(env.getProperty("token.expiration_hours")));
        Date date = Timestamp.valueOf(localDateTime);

        String token = Jwts.builder()
                .setSubject(userDetail.getUserId())
                .setExpiration(date)
                .signWith(SignatureAlgorithm.HS512, env.getProperty("token.secret"))
                .compact();

        response.addHeader("token", token);
        response.addHeader("userId", userDetail.getUserId());
    }
}
