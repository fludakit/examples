package com.example;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Attachments;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.MailSettings;
import com.sendgrid.helpers.mail.objects.Setting;
import io.github.fludakit.mail.MailAttachment;
import io.github.fludakit.mail.MailException;
import io.github.fludakit.mail.MailMessage;
import io.github.fludakit.mail.MailSender;

import java.io.InputStream;
import java.util.Base64;

/**
 * SendGrid implementation of MailSender.
 */
public class SendGridMailSender implements MailSender {

    private final String apiKey;
    private final SendGrid sg;
    private final boolean sandbox;

    public SendGridMailSender(String apiKey) {
        this(apiKey, false);
    }

    public SendGridMailSender(String apiKey, boolean sandbox) {
        this.apiKey = apiKey;
        this.sandbox = sandbox;
        this.sg = new SendGrid(apiKey);
    }

    @Override
    public void send(MailMessage emailMessage) throws MailException {

        Mail mail = buildMail(emailMessage);

        if (emailMessage.getTo().size() > 1) {
            for (int i = 1; i < emailMessage.getTo().size(); i++) {
                mail.getPersonalization().getFirst().addTo(new Email(emailMessage.getTo().get(i)));
            }
        }

        for (MailAttachment attachment : emailMessage.getAttachments()) {
            try (InputStream is = attachment.toDataSource().getInputStream()) {
                byte[] bytes = is.readAllBytes();
                String base64Content = Base64.getEncoder().encodeToString(bytes);

                Attachments sdkAttachment = new Attachments();
                sdkAttachment.setContent(base64Content);
                sdkAttachment.setType(attachment.toDataSource().getContentType());
                sdkAttachment.setFilename(attachment.getFilename());
                sdkAttachment.setDisposition("attachment");

                mail.addAttachments(sdkAttachment);
            } catch (Exception e) {
                throw new MailException("Failed attachment translation for SendGrid SDK", e);
            }
        }


        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = this.sg.api(request);
            if (response.getStatusCode() < 200 || response.getStatusCode() >= 300) {
                throw new MailException("SendGrid rejected payload: " + response.getStatusCode());
            }
        } catch (Exception ex) {
            throw new MailException("Fatal SendGrid execution runtime error", ex);
        }
    }

    private Mail buildMail(MailMessage emailMessage) {
        Email from = new Email(emailMessage.getFrom());
        String subject = emailMessage.getSubject();
        Content content = new Content("text/html", emailMessage.getHtmlBody());

        Email firstTo = new Email(emailMessage.getTo().get(0));
        Mail mail = new Mail(from, subject, firstTo, content);

        if (sandbox) {
            MailSettings mailSettings = new MailSettings();
            Setting sandboxMode = new Setting();
            sandboxMode.setEnable(true);
            mailSettings.setSandboxMode(sandboxMode);
            mail.setMailSettings(mailSettings);
        }
        return mail;
    }
}
