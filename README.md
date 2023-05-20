# Spring Cloud로 개발하는 마이크로서비스 애플리케이션(MSA)

# Section 1: Service Discovery

<img width="1218" alt="1" src="https://github.com/slrslrr2/Spring_Cloud_MSA/assets/58017318/2f94027e-0142-444e-be2e-8a0d94cfc12e">

MSA의경우, 하나의 서비스에 집중하고있다.

LocaBalancer(API Gateway)를 통해 들어온 요청정보를가지고 Spring Discovery에게 전달하면
Spring Discovery는 MS의 정보를 갖고있기에 요청된 경로의 서버를 검색해준다.

- 서비스 등록 및 검색기능
- ex) Netflix Eureka

| MS ID | MS 위치 정보 |
| --- | --- |
|  |  |
|  |  |

---

## Eureka Service Discovery 프로젝트 생성

Create New Project → Spring Initializr

Dependencies

- Spring Boot → `2.7.13`
- Spring Clout Discovery > Eureka Server

application.yml

```jsx
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

Application.java

```jsx
@SpringBootApplication
@EnableEurekaServer
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
```

위 프로젝트를 실행시키면 console에 아래와 같이 표시된다

이는 아직 eureka에 연동된 MicroService가 존재하지 않기때문이다.

```jsx
2023-05-20 11:01:37.230  INFO 69387 --- [           main] com.example.ecommerce.Application        : 
No active profile set, falling back to 1 default profile: "default"
```

<img width="863" alt="2" src="https://github.com/slrslrr2/Spring_Cloud_MSA/assets/58017318/749fd82f-f8d0-40d0-8228-c02374723d5c">
---

## User Service 프로젝트 생성

<img width="795" alt="3" src="https://github.com/slrslrr2/Spring_Cloud_MSA/assets/58017318/3774d460-e16c-4001-a50f-55893e72d667">
아래 dependencies를 추가한다.

<img width="795" alt="4" src="https://github.com/slrslrr2/Spring_Cloud_MSA/assets/58017318/e6daa164-ff96-450e-9e31-8b4ade12a4d7">

appication.yml

```jsx
server:
  port: 9001

spring:
  application:
    name: user-service

eureka:
  client:
    register-with-eureka: true
    fetch-registry: true # EUREKA 서버로부터 인스턴스들의 정보를 주기적으로 가져올것
    service-url:
      defaultZone: http://127.0.0.1:8761/eureka
```

UserServiceApplication.java

```
@SpringBootApplication
@EnableDiscoveryClient
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }

}

```

이렇게 EUREKA 서버를 등록하고 난후, EUREKA 서버로 들어가보면 EUREKA SERVICE가 등록됨을 확인할 수 있다.

<img width="860" alt="5" src="https://github.com/slrslrr2/Spring_Cloud_MSA/assets/58017318/f8374db1-d621-44ff-b4ec-35701e97ec42">
---

## User Service - 등록

UserSerice Application에서 port를 `9002`로 변경하여 하나 더 실행시킨다.

<img width="960" alt="6" src="https://github.com/slrslrr2/Spring_Cloud_MSA/assets/58017318/aaf0d9a9-becf-4440-8c4a-8541e7f324a5">

<img width="771" alt="7" src="https://github.com/slrslrr2/Spring_Cloud_MSA/assets/58017318/9dc70976-0f05-4a78-8627-dfa118d20efa">

다음과 같이 USER-SERVICE 의 2가지 서비스가 등록됨을 확인할 수 있다.

터미널을 열어 아래와 같이 9003 port로 또 실행시킨다.

```jsx
mvn spring-boot:run -Dspring-boot.run.jvmArguments='-Dserver.port=9003'
```

```jsx
mvn clean
mvn compile package
```

위와같이 실행 시 

`~/target/user-service-0.0.1-SNAPSHOT.jar` 해당 경로에 jar 파일이 생성된다

```jsx
java -jar -Dserver.port=9004 ./target/user-service-0.0.1-SNAPSHOT.jar
```

<img width="644" alt="8" src="https://github.com/slrslrr2/Spring_Cloud_MSA/assets/58017318/e5519ae8-d2fb-46b4-8c01-454216b11106">
---

## User Service -Load Balancer

만약 application.yml에서 port를 0으로 설정하면 랜덤하게 포트가 실행된다

```jsx
server:
  port: 0
#  port: 9001
```

그러나, instance status가 충돌날 수 있으므로,

아래와 같이 변경한다.

