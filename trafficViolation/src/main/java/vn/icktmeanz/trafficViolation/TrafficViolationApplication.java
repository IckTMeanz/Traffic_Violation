package vn.icktmeanz.trafficViolation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class TrafficViolationApplication {

	public static void main(String[] args) {
		SpringApplication.run(TrafficViolationApplication.class, args);
	}

}
