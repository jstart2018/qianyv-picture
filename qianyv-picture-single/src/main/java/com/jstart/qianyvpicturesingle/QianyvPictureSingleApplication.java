package com.jstart.qianyvpicturesingle;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@MapperScan("com.jstart.qianyvpicturesingle.mapper")
@EnableAspectJAutoProxy(exposeProxy = true)
public class QianyvPictureSingleApplication {

    public static void main(String[] args) {
        SpringApplication.run(QianyvPictureSingleApplication.class, args);
    }

}
