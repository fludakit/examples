package com.example;

import io.github.fludakit.mail.MailException;
import io.github.fludakit.mail.MailMessage;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Integration test for {@link SendGridMailSender} using SendGrid's Sandbox Mode.
 */
@Disabled
class SendGridMailSenderTest {

    @Test
    void sendMailInSandboxMode() {
        String apiKey = System.getenv("SENDGRID_API_KEY");
        SendGridMailSender sender = new SendGridMailSender(apiKey, true);

        MailMessage message = new MailMessage();
        message.setFrom("sender@example.com");
        message.addTo("receiver@example.com");
        message.setSubject("Test Subject - Sandbox Mode");
        message.setHtmlBody("<p>This is a test email sent in SendGrid sandbox mode.</p>");

        assertDoesNotThrow(() -> sender.send(message));
    }

    @Test
    void sendMailWithInvalidEmailThrowsMailException() {
        String apiKey = System.getenv("SENDGRID_API_KEY");
        SendGridMailSender sender = new SendGridMailSender(apiKey, true);

        MailMessage message = new MailMessage();
        message.setFrom("invalid-email");
        message.addTo("also-invalid");
        message.setSubject("Test Subject");
        message.setHtmlBody("<p>This should fail.</p>");

        assertThrows(MailException.class, () -> sender.send(message));
    }
}
