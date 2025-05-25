package com.example.campusbuddy;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@SpringBootApplication
@ServletComponentScan(basePackages = "com.example.campusbuddy.websocket")
@MapperScan("com.example.campusbuddy.mapper") // 添加 MapperScan 注解
public class CampusbuddyApplication {

	public static void main(String[] args) {
		SpringApplication.run(CampusbuddyApplication.class, args);
	}

}