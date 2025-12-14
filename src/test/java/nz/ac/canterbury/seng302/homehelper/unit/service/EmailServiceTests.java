package nz.ac.canterbury.seng302.homehelper.unit.service;

import jakarta.mail.internet.MimeMessage;
import nz.ac.canterbury.seng302.homehelper.config.AppConfig;
import nz.ac.canterbury.seng302.homehelper.entity.user.ForgottenPasswordToken;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.entity.user.VerificationToken;
import nz.ac.canterbury.seng302.homehelper.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestPropertySource(properties = {"spring.mail.username=test@example.com"})
public class EmailServiceTests {
    @Mock
    private JavaMailSender mailSender;

    @Mock
    private SpringTemplateEngine templateEngine;

    @Mock
    private AppConfig appConfig;

    @InjectMocks
    private EmailService emailService;

    @Captor
    private ArgumentCaptor<MimeMessage> messageCaptor;

    private User testUser;
    private ForgottenPasswordToken testForgottenPasswordToken;
    private VerificationToken testVerificationToken;

    @BeforeEach
    void setup() {
        testUser = new User("John", "Smith", "john.smith@gmail.com", "password", "password");
        testForgottenPasswordToken = new ForgottenPasswordToken(testUser, LocalDateTime.now().plusMinutes(10));
        testVerificationToken = new VerificationToken("123456", testUser, LocalDateTime.now().plusMinutes(10));
        testForgottenPasswordToken.setId(UUID.randomUUID());

        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // there must be a better way to do this...
        ReflectionTestUtils.setField(emailService, "fromEmail", "test@example.com");
    }

    @Test
    void testSendResetPasswordMail_callsSendHtmlTemplateEmail_WithCorrectParameters() {
        String html = "<html><body><h1>Forgotten Password</h1></body></html>";
        when(templateEngine.process(eq("email/forgottenPasswordMailTemplate.html"), any(Context.class)))
                .thenAnswer(invocation -> html);
        when(appConfig.getFullBaseUrl()).thenReturn("https://home-helper.nz");

        EmailService spyEmailService = Mockito.spy(emailService);
        spyEmailService.sendResetPasswordMail(testUser, testForgottenPasswordToken);

        String resetLink = "https://home-helper.nz/reset-password?token=" + testForgottenPasswordToken.getId().toString();
        Map<String, Object> model = Map.of("resetLink", resetLink);
        verify(spyEmailService, times(1)).sendHtmlTemplateEmail(
                eq("email/forgottenPasswordMailTemplate.html"),
                eq(testUser.getEmail()),
                eq("HomeHelper Forgotten Password"),
                eq(model));
    }

    @Test
    void testSendRegistrationVerificationMail_callsSendHtmlTemplateEmail_WithCorrectParameters() {
        String html = "<html><body><h1>Registration Verification</h1></body></html>";
        when(templateEngine.process(eq("email/registerVerificationMailTemplate.html"), any(Context.class)))
                .thenAnswer(invocation -> html);
        when(appConfig.getFullBaseUrl()).thenReturn("https://home-helper.nz");

        EmailService spyEmailService = Mockito.spy(emailService);
        spyEmailService.sendRegistrationVerificationMail(testUser, testVerificationToken);

        String verifyLink = "https://home-helper.nz/verification";
        Map<String, Object> model = Map.of(
                "verificationCode", testVerificationToken.getToken(),
                "verifyLink", verifyLink
        );
        verify(spyEmailService, times(1)).sendHtmlTemplateEmail(
                eq("email/registerVerificationMailTemplate.html"),
                eq(testUser.getEmail()),
                eq("HomeHelper Verification Code"),
                eq(model));
    }

    @Test
    void testSendPasswordUpdateMail_callsSendHtmlTemplateEmail_WithCorrectParameters() {
        String html = "<html><body><h1>Password Update</h1></body></html>";
        when(templateEngine.process(eq("email/passwordUpdatedMailTemplate.html"), any(Context.class)))
                .thenAnswer(invocation -> html);

        EmailService spyEmailService = Mockito.spy(emailService);
        spyEmailService.sendPasswordUpdatedMail(testUser);

        Map<String, Object> model = Map.of();
        verify(spyEmailService, times(1)).sendHtmlTemplateEmail(
                eq("email/passwordUpdatedMailTemplate.html"),
                eq(testUser.getEmail()),
                eq("HomeHelper Password Updated"),
                eq(model));
    }

    @Test
    void testSendHtmlTemplateEmail_Success() {
        String template = "email/testTemplate.html";
        String to = "recipient@example.com";
        String subject = "Test Subject";
        Map<String, Object> model = Map.of("key", "value");
        when(templateEngine.process(eq(template), any(Context.class)))
                .thenReturn("<html><body><h1>Email Content</h1></body></html>");

        emailService.sendHtmlTemplateEmail(template, to, subject, model);
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }
}
