package com.pravallika.student_preparation_tracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class StudentPreparationTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(
                StudentPreparationTrackerApplication.class,
                args
        );
    }
}