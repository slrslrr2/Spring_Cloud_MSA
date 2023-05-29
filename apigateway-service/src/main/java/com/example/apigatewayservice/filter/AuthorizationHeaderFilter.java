package com.example.apigatewayservice.filter;

import com.netflix.discovery.converters.Auto;
import io.jsonwebtoken.Jwts;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class AuthorizationHeaderFilter extends AbstractGatewayFilterFactory<AuthorizationHeaderFilter.Config> {
    @Autowired
    private Environment env;

    public AuthorizationHeaderFilter(){
        super(Config.class);
    }

    public static class Config{
    }

    // login -> token ->user (with token) -> header(include token)
    @Override
    public GatewayFilter apply(Config config) {

        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            if(!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return onError(exchange, "No authorization header", HttpStatus.UNAUTHORIZED);
            }

            String authorization = request.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
            String token = authorization.replace("Bearer ", "");

            if(!isJwtValid(token)) {
                return onError(exchange, "No token is not valid", HttpStatus.UNAUTHORIZED);
            }

            return chain.filter(exchange).then(Mono.fromRunnable(() -> {}));
        };
    }

    private boolean isJwtValid(String token) {
        boolean returnValue = true;
        String subject = null;

        try {

            subject = Jwts.parser()
                    .setSigningKey(env.getProperty("token.secret"))
                    .parseClaimsJws(token).getBody()
                    .getSubject();

        } catch (Exception e) {
            returnValue = false;
        }

        if(subject == null || subject.isEmpty() ){
            returnValue = false;
        }

        return returnValue;
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        log.error(err);
        return response.setComplete();
    }
}
