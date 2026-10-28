package com.example;

import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;
import jakarta.mail.MailSessionDefinition;
import jakarta.mail.Session;
import io.github.fludakit.mail.cdi.JakartaMail;

@ApplicationScoped
@MailSessionDefinition(
    name = "java:app/mail/mySession",
    host = "localhost",
    from = "noreply@example.com",
    transportProtocol = "smtp",
    properties = {
        "mail.smtp.port=1025",
        "mail.smtp.starttls.enable=false",
        "mail.smtp.auth=false"
    }
)
public class MailSetup {

    @Resource(lookup = "java:app/mail/mySession")
    private Session mailSession;

    @Produces
    @Singleton
    @JakartaMail
    public Session getMailSession() {
        return mailSession;
    }
}
