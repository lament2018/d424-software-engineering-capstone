package com.schedule.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    @Autowired private JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }
    public void sendNewUserEmail(String to, String subject,String text)
    {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            message.setFrom("uclinhdo@gmail.com");
            mailSender.send(message);
            System.out.println("Email sent to: " + to);
        } catch (Exception e) {
            System.err.println("Failed to send email to: " + to);
            e.printStackTrace();  // This will show the full reason in the console
        }
    }
}
