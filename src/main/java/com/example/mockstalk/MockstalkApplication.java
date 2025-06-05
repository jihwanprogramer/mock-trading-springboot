package com.example.mockstalk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing
@EnableCaching //레디스 캐싱을 활요하기위해 활성화
@EnableScheduling
public class MockstalkApplication {

	public static void main(String[] args) {
		SpringApplication.run(MockstalkApplication.class, args);
	}

}
