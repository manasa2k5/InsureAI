package com.insureai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableCaching
@EnableJpaRepositories(basePackages = "com.insureai.repository")
public class InsureaiApplication {

	public static void main(String[] args) {
		SpringApplication.run(InsureaiApplication.class, args);
	}

}
