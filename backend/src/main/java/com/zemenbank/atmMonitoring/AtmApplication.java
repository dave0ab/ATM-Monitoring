package com.zemenbank.atmMonitoring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.zemenbank.atmMonitoring")
@EnableScheduling
public class AtmApplication {

	public static void main(String[] args) {

		System.setProperty("networkaddress.cache.ttl", "0");
		System.setProperty("networkaddress.cache.negative.ttl", "0");
		SpringApplication.run(AtmApplication.class, args);
	}
}

