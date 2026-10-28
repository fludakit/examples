package com.example;

import io.github.fludakit.mail.MailSender;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.mail.Session;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.container.annotation.ArquillianTest;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.logging.Level;
import java.util.logging.Logger;

@ArquillianTest
public class MailSenderIT {
    private static final Logger LOGGER = Logger.getLogger(MailSenderIT.class.getName());

    @Inject
    private Session mailSession;

    @Inject
    private MailSender mailSender;

    @Inject
    private MailService mailService;

    @Deployment
    public static WebArchive createDeployment() {
        var libs = Maven.resolver().loadPomFromFile("pom.xml")
                .resolve(
                    "io.github.fludakit:fluda-mail-core",
                    "io.github.fludakit:fluda-mail-cdi"
                )
                .withTransitivity()
                .asFile();
        WebArchive archive = ShrinkWrap.create(WebArchive.class, "test-mail.war")
                .addAsLibraries(libs)
                .addPackages(true, "com.example")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        LOGGER.log(Level.INFO, "deployment archive: {0}", archive.toString(true));
        return archive;
    }

    @Test
    public void testMailSessionInjected() {
        Assertions.assertNotNull(mailSession, "MailSession should be injected");
    }

    @Test
    public void testMailSenderInjected() {
        Assertions.assertNotNull(mailSender, "MailSender should be injected");
    }

    @Test
    public void testSendEmail() {
        Assertions.assertDoesNotThrow(() -> {
            mailService.sendWelcomeEmail("test@example.com", "Test User");
        }, "Sending email should not throw an exception");
    }

    @Test
    public void testEmailReceivedByMailpit() throws Exception {
        mailService.sendWelcomeEmail("mailpit-test@example.com", "Mailpit User");

        Thread.sleep(2000);

        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8025/api/v1/messages"))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        Assertions.assertEquals(200, response.statusCode(), "Mailpit API should return 200");

        JsonObject jsonResponse = Json.createReader(new java.io.StringReader(response.body())).readObject();
        JsonArray messages = jsonResponse.getJsonArray("messages");

        Assertions.assertNotNull(messages, "Mailpit should return messages array");
        Assertions.assertTrue(messages.size() > 0, "Mailpit should have received at least one message");

        boolean found = false;
        for (int i = 0; i < messages.size(); i++) {
            JsonObject msg = messages.getJsonObject(i);
            JsonArray toArray = msg.getJsonArray("To");
            if (toArray != null && !toArray.isEmpty()) {
                JsonObject to = toArray.getJsonObject(0);
                String toAddress = to.getString("Address");
                if ("mailpit-test@example.com".equals(toAddress)) {
                    found = true;
                    Assertions.assertEquals("Welcome to FluDa Kit", msg.getString("Subject"));
                    break;
                }
            }
        }
        Assertions.assertTrue(found, "Email to mailpit-test@example.com should be found in mailpit");
    }
}
