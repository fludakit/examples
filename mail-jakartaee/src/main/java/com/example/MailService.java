package com.example;

import io.github.fludakit.mail.MailBuilder;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

@Dependent
public class MailService {

    @Inject
    MailBuilder mailBuilder;

    public void sendWelcomeEmail(String to, String userName) {
        mailBuilder
            .from("noreply@example.com")
            .to(to)
            .subject("Welcome to FluDa Kit")
            .htmlBody("<h1>Welcome, " + userName + "!</h1><p>Thank you for joining FluDa Kit.</p>")
            .send();
    }
}
