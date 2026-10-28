package com.example;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.container.annotation.ArquillianTest;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URL;
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
                .addPackages(true, "com.example")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        LOGGER.log(Level.INFO, "deployment archive: {0}", archive.toString(true));
        return archive;
    }

    @Test
    public void testRestApiSendEmail() throws Exception {
        Client client = ClientBuilder.newClient();
        try {
            MailResource.EmailRequest request = new MailResource.EmailRequest(
                "rest-test@example.com",
                "REST User"
            );

            Response response = client.target(deploymentUrl.toString())
                    .path("api/mail/send")
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.json(request));

            Assertions.assertEquals(200, response.getStatus(), "REST API should return 200");

            MailResource.EmailResponse emailResponse = response.readEntity(MailResource.EmailResponse.class);
            Assertions.assertNotNull(emailResponse.message(), "Response should contain a message");
            Assertions.assertTrue(emailResponse.message().contains("rest-test@example.com"),
                "Response should confirm email was sent");
        } finally {
            client.close();
        }
    }
}
