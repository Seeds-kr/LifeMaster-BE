package com.example.LifeMaster_BE;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.example.LifeMaster_BE.FunctionManager.ToDoList.TodoService;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication  // Spring Boot 애플리케이션 설정
@RestController
@ServletComponentScan
@EnableScheduling
public class LifeMasterBeApplication {

	private final TodoService todoService;
	@Autowired
	public LifeMasterBeApplication(TodoService todoService) {
		this.todoService = todoService;
	}
	public static void main(String[] args) {
		SpringApplication.run(LifeMasterBeApplication.class, args);  // 애플리케이션 실행
	}
	@GetMapping("/hello")
	public ResponseEntity<String> testApi() {
		String result = "APP 정상 실행";
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

}