package com.file.poller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.support.SpringBootServletInitializer;


@SpringBootApplication
public class FilePollerWacherApplication extends SpringBootServletInitializer implements CommandLineRunner{
	
	@Value("${file.source}")
    private String fileSource;
	
	@Value("${file.intermediate}")
    private String fileIntermediate;
	
	@Value("${file.target}")
    private String fileTarget;
	
	@Value("${file.sheet}")
    private String fileSheet;
	
	@Value("${name}")
    private String name;

	@Value("${upload.url}")
    private String uploadUrl;
	
	@Value("${threadSleepTime}")
	private Long threadSleepTime;

	public static void main(String[] args) {
		SpringApplication.run(FilePollerWacherApplication.class, args);
		System.out.println("Welcome to FilePollerWacherApplication main method");
	}

	@Override
	public void run(String... args) throws Exception {

		System.out.println("Welcome "+name);
		
		FilePollerWacher filePollerWacher = new FilePollerWacher(fileSource, fileIntermediate, fileTarget, fileSheet, uploadUrl, threadSleepTime);
	    filePollerWacher.fileWatcher();
	}

}
