# Section 2: API Gateway Service

Section 2: API Gateway Service

![Untitled](https://slrslrr1.notion.site/image/https%3A%2F%2Fs3-us-west-2.amazonaws.com%2Fsecure.notion-static.com%2F0c19fa66-97fa-4ed5-a38d-193971e973e1%2FUntitled.png?id=0ef1eda1-25ca-464d-9f06-582987c0da29&table=block&spaceId=510411ac-eb98-4d23-9324-2e8118f124da&width=1730&userId=&cache=v2)

1. 사용자 외부 요청을 단일화할 수 있도록 도와주는 API Gateway Service
2. Netflix Ribbon과 Zuul
3. Spring Cloud Gateway - (기본, Filter, Eureka연동, Load Balancer)

## API Gateway란?

사용자가 설정한 라우팅 설정에 따라 각 엔드포인트로 클라이언트 대신 MS설정을 확인하여 Client에게 다시 전달해줄 수 있는 Proxy역할을 하고있다.

**역할** 

- 인증 및 권한 부여
- 서비스 검색 통합
- 응답 캐싱
- 부하분산
- 로킹 추적, 상관관계 (클라이언트 요청 헤더, 쿼리스트링 청구)
- IP 허용 목록에 추가

![Untitled](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/c708b23d-5f19-4f31-bc2d-5a2836c95dbb/Untitled.png)

클라이언트 요청이 들어온경우 API Gateway를 통해 진입 후 MS에게 요청을 전달 및 응답을 받고 다시 클라이언트에게 전달한다.

![Untitled](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/fba7cc7d-b366-4cea-a2bc-6e2447acd0e3/Untitled.png)

## Netflix Ribbon

- Spring Cloud에서의 MSA간 통신
    - RestTemplate
    
    ```jsx
    RestTemplate restTemplate = new RestTemplate();
    restTemplate.getForObject("http://localhost:8080/", User.class, 200);
    ```
    
    - Feign Client
        - RestTemplate보다는 직접적인 URL 노출이 적다
    
    ```jsx
    @FeignClient("stores")
    public interface StoreClien{
    	@RequestMapping(method = RequestMethod.GET, value = "/stores") List<Store> getStores();
    }
    ```
    
- Ribbon: Client side Load Balancer
    - 서비스 이름으로 호출되고, Health Check를 할 수 있는 장점이 있지만
    비동기 방식이 불가능하다.
    - 현재는 **Maintenance** 상태

## Netfliz Zuul

![Untitled](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/9aac2ff0-e2f1-4ee4-8e20-a2590566e0b1/Untitled.png)

하지만, Netflix Zuul역시 Spring Boot 2.4에서는 **Maintenance** 상태이다.

![Untitled](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/5a276ef5-7793-4cbe-a156-298ee3c11acf/Untitled.png)

그렇기에 현재는 위와같이 권장하고있다.

[https://spring.io/blog/2018/12/12/spring-cloud-greenwich-rc1-available-now#spring-cloud-netflix-projects-entering-maintenance-mode](https://spring.io/blog/2018/12/12/spring-cloud-greenwich-rc1-available-now#spring-cloud-netflix-projects-entering-maintenance-mode)

---

## Spring Cloud Gateway

Spring Cloud Gateway를 통하여 API Gateway를 만들어보도록 하자.

Spring Cloud Gateway는 

Netflix Ribbon, Netfliz Zuul보다 장점이 비동기방식을 지원하며, Spring과의 라이브러리 호환성이 좋다.

Spring Cloud Gateway를 사용하게되면, Netty 내장서버의 경우 비동기방식을 지원해준다

일단 간단하게 first-service, second-service 2가지를 만든다.

![Untitled](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/875a3bf6-bc79-42f5-9006-fcdc6fd50db9/Untitled.png)

![Untitled](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/cc9a29a3-1182-46c7-a534-3891a84032b7/Untitled.png)

application.yml

```jsx
server:
  port: 8081

spring:
  application:
    name: my-first-service

eureka:
  client:
    register-with-eureka: false
    fetch-registry: false
```

FirstServiceController

```jsx
@RestController
@RequestMapping("/first-service")
public class FirstServiceController {

    @GetMapping("/welcome")
    public String welcome(){
        return "Welcome to the First service";
    }
}
```

위와같은 방식으로 8082로 Second Service도 만들어놓는다

application.yml

```jsx
server:
  port: 8082

spring:
  application:
    name: my-second-service

eureka:
  client:
    register-with-eureka: false
    fetch-registry: false
```

SecondServiceController

```jsx
@RestController
@RequestMapping("/second-service")
public class SecondServiceController {

    @GetMapping("/welcome")
    public String welcome(){
        return "Welcome to the Second service";
    }
}
```

## 그리고 spring cloud api gateway를 만들어보자

![Untitled](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/8f0b2ab4-4a3f-4e5c-af5b-7d10f0a285f5/Untitled.png)

**Gateway**, **Eureka Discovery Client**, Lombok 3가지를 설정한다.

![Untitled](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/092efed9-f053-47f7-8e58-f9eb78e4f333/Untitled.png)

application.yml

```yaml
server:
  port: 8000

eureka:
  client:
    register-with-eureka: false
    fetch-registry: false
    service-url:
      defaultZone: http://localhost:8761/eureka

spring:
  application:
    name: apigateway-service
  cloud:
    gateway:
      routes:
        - id: first-service
          uri: http://localhost:8081/
          predicates:
            - Path=/first-service/**
        - id: second-service
          uri: http://localhost:8082/
          predicates:
            - Path=/second-service/**
```

위 설정에 대한 프로젝트를 만든 후 실행하면 아래와같은 Log를 발견할 수 있다.

```jsx
o.s.b.web.embedded.netty.NettyWebServer  : Netty started on port 8000
```

[http://localhost:8000/first-service/welcome](http://localhost:8000/first-service/welcome) 해당 api gateway로 도메인 요청 시 

spring cloud api로 인하여 first-service인 localhost:8081로 이동하여 다음과 같은 화면이 표시된다

![Untitled](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/92181d93-9c32-42db-b002-d6c780a24596/Untitled.png)

![Untitled](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/e0958cf8-bfee-4865-b819-25ce1faf4ccc/Untitled.png)

---

## Spring Cloud Gateway - Filter

![Untitled](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/d2b02a08-fe94-49cf-97cb-bc626ca9c3f3/Untitled.png)

Client가 SpringCloud gateway로 요청을 전달하면 First Service와 Second Service로 요청에 알맞게 분기처리한다.

자세히 보면 Spring Cloud gateway에서 사전처리인 Pre Filter와 사후처리인 Post Filter를 처리할 수 있다.

이는 Properties에서, Java Code에서 적용할 수 있다.

### 선처리 작업

first-service - FirstServiceController

```java
@RestController
@RequestMapping("/first-service")
@Slf4j
public class FirstServiceController {

    @GetMapping("/welcome")
    public String welcome(){
        return "Welcome to the First service";
    }

    @GetMapping("/message")
    public String message(@RequestHeader("first-request") String header){
        log.info(header);
        return "Hello World in First Service";
    }
}
```

second-service - SecondServiceController

```java
@RestController
@RequestMapping("/second-service")
@Slf4j
public class SecondServiceController {

    @GetMapping("/welcome")
    public String welcome(){
        return "Welcome to the Second service";
    }

    @GetMapping("/message")
    public String message(@RequestHeader("second-request") String header){
        log.info(header);
        return "Hello World in second Service";
    }
}
```

### 방법1:  Java로 Filter 만들기

apigateway-service: application.yml

```yaml
server:
  port: 8000

eureka:
  client:
    register-with-eureka: false
    fetch-registry: false
    service-url:
      defaultZone: http://localhost:8761/eureka

spring:
  application:
    name: apigateway-service
#  cloud:
#    gateway:
#      routes:
#        - id: first-service
#          uri: http://localhost:8081/
#          predicates:
#            - Path=/first-service/**
#          filters:
#            - AddRequestHeader=first-request, first-request-header2
#            - AddResponseHeader=first-response, first-response-header2
#        - id: second-service
#          uri: http://localhost:8082/
#          predicates:
#            - Path=/second-service/**
#          filters:
#            - AddRequestHeader=second-request, second-request-header2
#            - AddResponseHeader=second-response, second-response-header2
```

apigateway-service: FilterConfig.java

```java
package com.example.apigatewayservice.config.apigatewayservice;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder builder){
        return builder.routes()
                .route(r -> r.path("/first-service/**") //라우터 등록
                        .filters(                                //필터 등록
                                f->f.addRequestHeader("first-request","first-request-header")   //ReqeustHeader 추가
                                        .addResponseHeader("first-response","first-response-header"))            //ResponseHeader 추가
                        .uri("http://localhost:8081")
                ).route(r -> r.path("/second-service/**")
                        .filters(
                                f->f.addRequestHeader("second-request","second-request-header")
                                        .addResponseHeader("second-response","second-response-header"))
                        .uri("http://localhost:8082")
                )
                .build();
    }
}
```

### 방법2:  yml(properties)로 Filter만들기

```yaml
server:
  port: 8000

eureka:
  client:
    register-with-eureka: false
    fetch-registry: false
    service-url:
      defaultZone: http://localhost:8761/eureka

spring:
  application:
    name: apigateway-service
  cloud:
    gateway:
      routes:
        - id: first-service
          uri: http://localhost:8081/
          predicates:
            - Path=/first-service/**
          filters:
            - AddRequestHeader=first-request, first-request-header2
            - AddResponseHeader=first-response, first-response-header2
        - id: second-service
          uri: http://localhost:8082/
          predicates:
            - Path=/second-service/**
          filters:
            - AddRequestHeader=second-request, second-request-header2
            - AddResponseHeader=second-response, second-response-header2
```

결과는 아래와 같이 header 값이 MicroService에 전달된다.

![Untitled](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/bd6915f2-620e-4f04-aaa4-5b97ac1ed8f6/Untitled.png)

---

## Spring Cloud Gateway - Custom Filter

### 선처리 작업

first-service - FirstServiceController

```java
@RestController
@RequestMapping("/second-service")
@Slf4j
public class SecondServiceController {

..........생략............

    @GetMapping("check")
    public String check(){
        return "Hi, there. This is a message from Second Service";
    }
}
```

```java
@RestController
@RequestMapping("/first-service")
@Slf4j
public class FirstServiceController {

..........생략............

    @GetMapping("check")
    public String check(){
        return "Hi, there. This is a message from First Service";
    }
}
```

### apigateway-service CustomFilter 만들기

application.yml

```yaml
server:
  port: 8000

eureka:
  client:
    register-with-eureka: false
    fetch-registry: false
    service-url:
      defaultZone: http://localhost:8761/eureka

spring:
  application:
    name: apigateway-service
  cloud:
    gateway:
      routes:
        - id: first-service
          uri: http://localhost:8081/
          predicates:
            - Path=/first-service/**
          filters:
#            - AddRequestHeader=first-request, first-request-header2
#            - AddResponseHeader=first-response, first-response-header2
            - CustomFilter
        - id: second-service
          uri: http://localhost:8082/
          predicates:
            - Path=/second-service/**
          filters:
#            - AddRequestHeader=second-request, second-request-header2
#            - AddResponseHeader=second-response, second-response-header2
            - CustomFilter
```

```java
@Component
@Slf4j
public class CustomFilter extends AbstractGatewayFilterFactory<CustomFilter.Config> {
    public CustomFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        // Custom Pre Filter
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();

            log.info("Custom PRE filter: request id -> {}", request.getId());

            // Custom POST Filter
            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                log.info("Custom POST filter: response code -> {}", response.getStatusCode());
            }));
        };
    }

    public static class Config{

    }
}
```

위 `Mono` 의 경우 WebFlux에서 제공하는 리액티브 타입 중 하나입니다.

**`Mono`**는 Spring WebFlux에서 사용되는 리액티브 타입 중 하나입니다. 리액티브 프로그래밍은 비동기 및 논블로킹 작업을 위해 사용되며, 리액티브 스트림을 통해 데이터를 처리하는 방식입니다.

**`Mono`**는 0 또는 1개의 결과를 가질 수 있는 리액티브 스트림을 나타냅니다. 즉, 단일 결과 값을 발행할 수 있습니다. **`Mono`**는 Publisher 인터페이스를 구현하고, 리액티브 스트림에서 데이터를 생성하고 변환하며 조작하는데 사용됩니다.

위의 코드에서 **`Mono.fromRunnable()`** 메서드는 **`Mono`**를 생성하는 메서드입니다. **`Mono.fromRunnable()`** 메서드는 주어진 Runnable(실행 가능한 작업)을 실행하고 완료되면 Mono를 발행합니다. 이 경우, **`Mono.fromRunnable(() -> { log.info("Custom POST filter: response code -> {}", response.getStatusCode()); })`** 코드는 비동기적으로 실행되는 작업으로서, 해당 작업이 완료되면 Mono를 발행합니다.

결과적으로, **`chain.filter(exchange).then(Mono.fromRunnable(...))`** 코드는 현재 필터 체인을 계속 진행시키고, 필터 체인이 완료된 후에 비동기적으로 실행되는 작업을 수행합니다. 이 작업은 **`Custom POST filter: response code -> ...`** 로그를 출력하는 역할을 수행합니다.

### 결과

apigateway-service

```yaml
2023-05-20 20:17:20.113  INFO 94520 --- [ctor-http-nio-2] c.e.a.filter.CustomFilter                : Custom PRE filter: request id -> 27bba8f8-1
2023-05-20 20:17:20.771  INFO 94520 --- [ctor-http-nio-2] c.e.a.filter.CustomFilter                : Custom POST filter: response code -> 200 OK
```

![Untitled](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/20dbe4e1-be5a-4ddb-a5ee-b2d9336c07d8/Untitled.png)

---

## Spring Cloud Gateway - Global Filter

application.yml

아래 내용을 추가한다.

```yaml
      default-filters:
        - name: GlobalFilter
          args:
            baseMessage: Spring Cloud Gateway Global Filter
            preLogger: true
            postLogger: true
```

```yaml
# 완성본
server:
  port: 8000

eureka:
  client:
    register-with-eureka: false
    fetch-registry: false
    service-url:
      defaultZone: http://localhost:8761/eureka

spring:
  application:
    name: apigateway-service
  cloud:
    gateway:
      default-filters:
        - name: GlobalFilter
          args:
            baseMessage: Spring Cloud Gateway Global Filter
            preLogger: true
            postLogger: true
      routes:
        - id: first-service
          uri: http://localhost:8081/
          predicates:
            - Path=/first-service/**
          filters:
#            - AddRequestHeader=first-request, first-request-header2
#            - AddResponseHeader=first-response, first-response-header2
            - CustomFilter
        - id: second-service
          uri: http://localhost:8082/
          predicates:
            - Path=/second-service/**
          filters:
#            - AddRequestHeader=second-request, second-request-header2
#            - AddResponseHeader=second-response, second-response-header2
            - CustomFilter
```

위 default-filters로 추가한 GlobalFilter를 추가한다.

```java
@Component
@Slf4j
public class GlobalFilter extends AbstractGatewayFilterFactory<GlobalFilter.Config> {
    public GlobalFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        // Global Pre Filter
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();

            log.info("Global PRE filter: baseMessage -> {}", config.getBaseMessage());

            if(config.isPreLogger()){
                log.info("Global Filter start: request.getId() -> {}", request.getId());
            }

            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                if(config.isPostLogger()){
                    log.info("Global Filter End: reponse code -> {}", response.getStatusCode());
                }
            }));
        };
    }

    @Data
    public static class Config{
        private String baseMessage; //application.yml에 값을 설정한다.
        private boolean preLogger;
        private boolean postLogger;
    }
}
```

### 결과: default-filters로 설정한 
          공통필터인 GlobalFilter가 먼저 작동 후 CustomFilter가 작동한다

![Untitled](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/5e7251f1-ca6e-4550-a48c-d124cb56c737/Untitled.png)

---

## Spring Cloud Gateway - Logging Filter

작업내용 : 이번엔 second-service에만 Logging Filter를 적용한다.

결과는 미리 알아보면 다음과 같다.

![Untitled](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/312992a6-f002-4076-9a38-d3ee5b99f256/Untitled.png)

![Untitled](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/495e1328-471d-456c-8862-f881987e8f57/Untitled.png)

apigateway-service - LoggingFilter

```java
@Component
@Slf4j
public class LoggingFilter extends AbstractGatewayFilterFactory<LoggingFilter.Config> {
    public LoggingFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        GatewayFilter filter = new OrderedGatewayFilter((exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();

            log.info("Logging filter: baseMessage -> {}", config.getBaseMessage());
            if(config.isPreLogger()){
                log.info("Logging PRE start: request.getId() -> {}", request.getId());
            }

            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                if(config.isPostLogger()){
                    log.info("Logging POST End: response code -> {}", response.getStatusCode());
                }
            }));
            
// Order 값을 Ordered.HIGHEST_PRECEDENCE 로 잡으면 Global Filter보다 먼저 실행된다.
//        }, Ordered.HIGHEST_PRECEDENCE); 
        }, Ordered.LOWEST_PRECEDENCE);

        return filter;
    }

    @Data
    public static class Config{
        private String baseMessage;
        private boolean preLogger;
        private boolean postLogger;
    }
}
```

---

## Spring Cloud Gateway - Eureka 연동

![Untitled](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/118a5687-7a47-49a7-a953-ea24c9d353c4/Untitled.png)

1) Client 요청이 들어오면 `API Gateway(8000)` 을 통해 처음 들어오게되며

2) Service Discovery Eureka Server(8761) 서버를 통하여 어디에 MicroService가 등록되었는지 알게 되고 이를 API Gateway에게 응답한다.

3) API Gateway 서비스가 MicroService에게 포워딩을 해주게된다.

총 프로젝트는 4가지이다.

1. eureka (8762)
2. apigateway (8000)
3. firstservice(8081), secondservice(8082)

1번은 pom.xml에 아래 dependecy가 존재해야한다

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
</dependency>
```

2,3에는 pom.xml에 아래 dependecy가 존재해야한다

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

또한 2,3에는 application.yml에 아래와같이 존재해야한다

```yaml
eureka:
  client:
    register-with-eureka: true
    fetch-registry: true
    service-url:
      defaultZone: http://localhost:8761/eureka
```

그럼 결과적으로 내용은 아래와 같다.

eureka - application.yml

```yaml
server:
  port: 8761

spring:
  application:
    name: discoveryservice

eureka:
  client:
    register-with-eureka: false
    fetch-registry: false
```

apigateway - application.yml

```yaml
server:
  port: 8000

eureka:
  client:
    register-with-eureka: true
    fetch-registry: true
    service-url:
      defaultZone: http://localhost:8761/eureka

spring:
  application:
    name: apigateway-service
  cloud:
    gateway:
      default-filters:
        - name: GlobalFilter
          args:
            baseMessage: Spring Cloud Gateway Global Filter
            preLogger: true
            postLogger: true
      routes:
        - id: first-service
					uri: lb://MY-FIRST-SERVICE ## 기존 localhost:8081이 아닌, spring.application.name을 적는다
          predicates:
            - Path=/first-service/**
          filters:
            - CustomFilter
        - id: second-service
          uri: lb://MY-SECOND-SERVICE
          predicates:
            - Path=/second-service/**
          filters:
            - name: CustomFilter
            - name: LoggingFilter
              args:
                baseMessage: Hi, there.
                preLogger: true
                postLogger: true
```

```yaml

routes:
  - id: first-service
		uri: lb://MY-FIRST-SERVICE ## 기존 localhost:8081이 아닌, spring.application.name을 적는다
    predicates:
```

위 routes에 localhost:8081이 아니라

apigateway에서 로드밸런싱을 해주기위해,

 

eureka에서 서버를검색해서 전해주기위한, 

MicroService의 spring.application.name을 적어놓는다

first-service

```yaml
server:
  port: 8081

spring:
  application:
    name: my-first-service

eureka:
  client:
    register-with-eureka: true
    fetch-registry: true
    service-url:
      defaultZone: http://localhost:8761/eureka
```

second-service

```yaml
server:
  port: 8082

spring:
  application:
    name: my-second-service

eureka:
  client:
    register-with-eureka: true
    fetch-registry: true
    service-url:
      defaultZone: http://localhost:8761/eureka
```

![Untitled](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/08f2da07-cce0-487f-a5fd-d362916a841a/Untitled.png)

---

## Spring Cloud Gateway - Load Balance

FirstService와 SecondService를 각각 2개씩 기동하는방법

![Untitled](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/aef16d78-df5f-4f10-a046-4c1b8ac7d366/Untitled.png)

아래와 같이 FirstService를 9091, SecondService를 9092로 실행시켜준다

![Untitled](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/95006cc8-b967-48a2-83da-0c05e8101c27/Untitled.png)

이렇게 된다면 외부 요청이 들어왔을 때, My-FIRST-SERVICE로 요청이 들어오게된다면

아래 어디로 가야하는지 알 수 있어야한다.

```xml
192.168.0.15:my-first-service:8081 , 192.168.0.15:my-first-service:9091
```

그렇기에 일단 MY-FIRST-SERVICE를 모두 종료해주고

port를 0으러 바꾸고 아래 내용 추가해준다

```xml
server:
  port: 0

eureka:
	instance:
    instance-id: ${spring.cloud.client.hostname}:${spring.application.instance_id:${random.value}}   ### 추가
```

하고 2번 실행해준다

- run 버튼
- mvn spring-boot:run

그리고 first-service에 아래와 같이 추가해준다

```java
@RestController
@RequestMapping("/first-service")
@Slf4j
public class FirstServiceController {
	Environment env;

	@Autowired
	public FirstServiceController(Environment env){
		this.env = env;
	}

		........... 생략 ..........

	@GetMapping("/check")
	public String check(HttpServletRequest request){
	  log.info("Server port={}", request.getServerPort());

    return String.format("Hi, there. This is a message from First Service %s", env.getProperty("local.server.port"));
	}
```

위 포트를 찍어줌으로써 어디에서 어떻게 데이터가 왔는지 확인 가능하다.

아래는 완성본
```java
@RestController
@RequestMapping("/first-service")
@Slf4j
public class FirstServiceController {
    Environment env;

    @Autowired
    public FirstServiceController(Environment env){
        this.env = env;
    }

    @GetMapping("/welcome")
    public String welcome(){
        return "Welcome to the First service";
    }

    @GetMapping("/message")
    public String message(@RequestHeader("first-request") String header){
        log.info(header);
        return "Hello World in First Service";
    }

    @GetMapping("/check")
    public String check(HttpServletRequest request){
        log.info("Server port={}", request.getServerPort());

        return String.format("Hi, there. This is a message from First Service %s", env.getProperty("local.server.port"));
    }
}
```
