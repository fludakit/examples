package com.example;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("mail")
@RequestScoped
public class MailResource {

    @Inject
    MailService mailService;

    @POST
    @Path("send")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response sendEmail(EmailRequest request) {
        mailService.sendWelcomeEmail(request.to(), request.userName());
        return Response.ok(new EmailResponse("Email sent to " + request.to())).build();
    }

    public record EmailRequest(String to, String userName) {}
    public record EmailResponse(String message) {}
}
