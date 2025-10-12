package com.avatar.avatar_online;

import com.avatar.avatar_online.raft.service.DatabaseSyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.xml.crypto.Data;

@EnableAsync
@EnableScheduling
@SpringBootApplication
public class AvatarOnlineApplication {
    public static void main(String[] args) {SpringApplication.run(AvatarOnlineApplication.class, args);}
}
