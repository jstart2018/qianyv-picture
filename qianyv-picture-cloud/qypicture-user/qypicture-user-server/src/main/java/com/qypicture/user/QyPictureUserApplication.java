package com.qypicture.user;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.qypicture.user.mapper")
public class QyPictureUserApplication {

    public static void main(String[] args) {
        SpringApplication.run(QyPictureUserApplication.class, args);
    }

}
