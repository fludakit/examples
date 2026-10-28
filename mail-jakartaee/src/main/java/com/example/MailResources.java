package com.example;

import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.mail.MailSessionDefinition;
import jakarta.mail.Session;

@ApplicationScoped
@MailSessionDefinition(
    name = "java:comp/mail/mySession",
    host = "localhost",
    from = "noreply@example.com",
    transportProtocol = "smtp",
    properties = {
        "mail.smtp.port=1025",
        "mail.smtp.starttls.enable=false",
        "mail.smtp.auth=false"
    }
)
public class MailResources {

    @Resource(lookup = "java:comp/mail/mySession")
    private Session mailSession;

    public Session getMailSession() {
        return mailSession;
    }
}
