package com.example.timetrackingservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableDiscoveryClient
@EnableScheduling
public class TimeTrackingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TimeTrackingServiceApplication.class, args);
    }

}
