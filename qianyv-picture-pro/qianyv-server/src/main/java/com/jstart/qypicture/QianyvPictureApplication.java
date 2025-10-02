package com.jstart.qypicture;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.jstart.qypicture.mapper")
public class QianyvPictureApplication {

    public static void main(String[] args) {
        SpringApplication.run(QianyvPictureApplication.class, args);
    }

}
