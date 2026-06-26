package com.bus.monitoringsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MonitoringsystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(MonitoringsystemApplication.class, args);
	}

}
