package com.campus2hand.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;

import jakarta.mail.internet.MimeMessage;
import java.io.InputStream;

@Configuration
@Profile("h2")
public class H2MailConfig {

    @Bean
    public JavaMailSender javaMailSender() {
        return new JavaMailSender() {
            @Override
            public MimeMessage createMimeMessage() {
                return null;
            }

            @Override
            public MimeMessage createMimeMessage(InputStream contentStream) throws MailException {
                return null;
            }

            @Override
            public void send(MimeMessage mimeMessage) throws MailException {
            }

            @Override
            public void send(MimeMessage... mimeMessages) throws MailException {
            }

            @Override
            public void send(MimeMessagePreparator mimeMessagePreparator) throws MailException {
            }

            @Override
            public void send(MimeMessagePreparator... mimeMessagePreparators) throws MailException {
            }

            @Override
            public void send(SimpleMailMessage simpleMessage) throws MailException {
            }

            @Override
            public void send(SimpleMailMessage... simpleMessages) throws MailException {
            }
        };
    }
}