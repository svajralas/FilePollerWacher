package com.file.poller;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.support.SpringBootServletInitializer;


@SpringBootApplication
public class FilePollerWacherApplication extends SpringBootServletInitializer{
	
	

	public static void main(String[] args) {
		SpringApplication.run(FilePollerWacherApplication.class, args);
		System.out.println("Welcome to FilePollerWacherApplication main method");
	}


}
