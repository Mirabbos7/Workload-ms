package org.example.workloadms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jms.annotation.EnableJms;

@SpringBootApplication
@EnableJms
public class WorkloadMsApplication {

    public static void main(String[] args) {
        SpringApplication.run(WorkloadMsApplication.class, args);
    }

}
