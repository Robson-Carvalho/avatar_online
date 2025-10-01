package com.avatar.avatar_online;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class AvatarOnlineApplication {

	public static void main(String[] args) {
		SpringApplication.run(AvatarOnlineApplication.class, args);
	}

}
