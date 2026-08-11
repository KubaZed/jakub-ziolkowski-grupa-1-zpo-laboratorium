package com.project;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ProjectMonolithApplication {

	private static final Logger logger = LoggerFactory.getLogger(ProjectMonolithApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(ProjectMonolithApplication.class, args);
	}

}