// 1. Main App Class
package com.schedule;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;


@SpringBootApplication
public class ScheduleApplication {
    public static void main(String[] args) {
        SpringApplication.run(ScheduleApplication.class, args);
        //String rawPassword = "12345678"; // what you're typing in the login form
        //String hashed = "$2a$10$0VDgEQtaXJbu1mTDXcMRdejDWLTHM70rHmvu1pd.8vrYjftp0X.Bi"; // from DB

        //BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        //boolean matches = encoder.matches(rawPassword, hashed);
        //System.out.println("Password match: " + matches);
    }
}