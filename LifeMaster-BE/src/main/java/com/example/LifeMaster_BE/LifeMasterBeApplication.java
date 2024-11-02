package com.example.LifeMaster_BE;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication  // Spring Boot 애플리케이션 설정
@RestController
public class LifeMasterBeApplication {

	public static void main(String[] args) {
		SpringApplication.run(LifeMasterBeApplication.class, args);  // 애플리케이션 실행
	}

	@GetMapping("/hello")
	public ResponseEntity<String> testApi() {
		String result = "api 정상 실행";
		return new ResponseEntity<>(result, HttpStatus.OK);
	}
}