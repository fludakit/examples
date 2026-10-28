package com.example;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.container.annotation.ArquillianTest;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.logging.Level;
import java.util.logging.Logger;

@ArquillianTest
public class MailResourceIT {
    private static final Logger LOGGER = Logger.getLogger(MailResourceIT.class.getName());

    @ArquillianResource
    private URL deploymentUrl;

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        var libs = Maven.resolver().loadPomFromFile("pom.xml")
                .resolve(
                    "io.github.fludakit:fluda-mail-core",
                    "io.github.fludakit:fluda-mail-cdi"
                )
                .withTransitivity()
                .asFile();
        WebArchive archive = ShrinkWrap.create(WebArchive.class, "mail-rest.war")
                .addAsLibraries(libs)
                .addClasses(MailSetup.class, MailService.class, MailResource.class, JaxRsApplication.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        LOGGER.log(Level.INFO, "deployment archive: {0}", archive.toString(true));
        return archive;
    }

    @Test
    public void testRestApiSendEmail() throws Exception {
        HttpClient httpClient = HttpClient.newHttpClient();
        
        String jsonBody = """
            {
                "to": "rest-test@example.com",
                "userName": "REST User"
            }
            """;
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(deploymentUrl.toString() + "api/mail/send"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        Assertions.assertEquals(200, response.statusCode(), "REST API should return 200");

        try (Jsonb jsonb = JsonbBuilder.create()) {
            var jsonResponse = jsonb.fromJson(response.body(), java.util.Map.class);
            String message = String.valueOf(jsonResponse.get("message"));
            Assertions.assertNotNull(message, "Response should contain a message");
            Assertions.assertTrue(message.contains("rest-test@example.com"),
                "Response should confirm email was sent");
        }
    }
}
