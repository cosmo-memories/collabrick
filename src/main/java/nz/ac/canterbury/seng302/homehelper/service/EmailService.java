package nz.ac.canterbury.seng302.homehelper.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import nz.ac.canterbury.seng302.homehelper.config.AppConfig;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Invitation;
import nz.ac.canterbury.seng302.homehelper.entity.user.ForgottenPasswordToken;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.entity.user.VerificationToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Service class responsible for handling email-related operations.
 */
@Service
public class EmailService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;
    private final AppConfig appConfig;

    // Gets From email from application.properties
    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * Constructs a {@code EmailService}
     *
     * @param mailSender     the Spring Mail object that handles email sending
     * @param templateEngine the Spring object that handles connecting variables to a html template for use in an email
     * @param appConfig      component to get the base URL the app is running on
     */
    @Autowired
    public EmailService(JavaMailSender mailSender, SpringTemplateEngine templateEngine, AppConfig appConfig) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.appConfig = appConfig;
    }

    /**
     * Sends a password reset email to the specified user with a password reset token.
     *
     * @param user  the user to send the email to.
     * @param token the password reset token associated with the user.
     */
    @Async
    public CompletableFuture<Void> sendResetPasswordMail(User user, ForgottenPasswordToken token) {
        String resetLink = appConfig.getFullBaseUrl() + "/reset-password?token=" + token.getId();
        Map<String, Object> model = Map.of("resetLink", resetLink);
        sendHtmlTemplateEmail("email/forgottenPasswordMailTemplate.html",
                user.getEmail(),
                "HomeHelper Forgotten Password",
                model);
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Sends a registration verification email to the specified user with a registration token.
     *
     * @param user  the user to send the email to.
     * @param token the verification token associated with the user.
     */
    @Async
    public CompletableFuture<Void> sendRegistrationVerificationMail(User user, VerificationToken token) {
        String verifyLink = appConfig.getFullBaseUrl() + "/verification";
        Map<String, Object> model = Map.of(
                "verificationCode", token.getToken(),
                "verifyLink", verifyLink);
        sendHtmlTemplateEmail("email/registerVerificationMailTemplate.html",
                user.getEmail(),
                "HomeHelper Verification Code",
                model);
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Sends a password updated email to the specified user.
     *
     * @param user the user to send the email to.
     */
    @Async
    public CompletableFuture<Void> sendPasswordUpdatedMail(User user) {
        Map<String, Object> model = Map.of();
        sendHtmlTemplateEmail("email/passwordUpdatedMailTemplate.html",
                user.getEmail(),
                "HomeHelper Password Updated",
                model);
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Sends an invitation email to its intended recipient.
     *
     * @param invitation Invitation object
     * @return a {@link CompletableFuture} that completes when the email has been sent.
     */
    @Async
    public CompletableFuture<Void> sendInvitationMail(Invitation invitation) {
        String invitationLink = appConfig.getFullBaseUrl() + "/invitation?token=" + invitation.getId();
        String declineLink = appConfig.getFullBaseUrl() + "/decline-invitation?token=" + invitation.getId();
        Map<String, Object> model = Map.of("invitationLink", invitationLink,
                "invitation", invitation,
                "declineLink", declineLink);
        sendHtmlTemplateEmail("email/invitationMailTemplate.html",
                invitation.getEmail(),
                "HomeHelper Renovation Invitation",
                model);
        return CompletableFuture.completedFuture(null);
    }


    /**
     * Sends a Html email using a template for styling
     *
     * @param emailTemplate The template the email uses
     * @param to            The email the message is sent to
     * @param subject       The subject of the email
     * @param model         The mapping of the variables from the service to the template
     */
    public void sendHtmlTemplateEmail(String emailTemplate, String to, String subject, Map<String, Object> model) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(new InternetAddress(fromEmail));
            helper.setTo(to);
            helper.setSubject(subject);

            // Sets variables from service to the html template (e.g. verification code)
            Context context = new Context();
            context.setVariables(model);
            String htmlBody = templateEngine.process(emailTemplate, context);
            helper.setText(htmlBody, true);
            mailSender.send(message);
            logger.info("Sent email");
        } catch (MessagingException exception) {
            logger.error("Failed to send email", exception);
        }
    }
}