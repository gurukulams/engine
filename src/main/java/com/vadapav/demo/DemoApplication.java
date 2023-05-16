package com.vadapav.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootVersion;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.SpringVersion;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
@CrossOrigin
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@RequestMapping("/api/platform")
    String platform() {
        return System.getProperty("java.vendor") + " @ " + System.getProperty("java.version");
    }

	@RequestMapping("/api/foundation")
	String foundation() {
		return "Spring Boot @ " + SpringBootVersion.getVersion();
	}

}
