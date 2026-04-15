package com.example.recompense.Service;

import jakarta.mail.Address;
import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "fromAddress", "no-reply@freelink.local");
    }

    @Test
    void sendBadgeEmail_shouldIgnoreBlankRecipient() {
        emailService.sendBadgeEmail("   ", "Subject", "Body", null, null);

        verify(mailSender, never()).createMimeMessage();
        verify(mailSender, never()).send(org.mockito.ArgumentMatchers.any(MimeMessage.class));
    }

    @Test
    void sendBadgeEmail_shouldBuildMimeMessageWithAttachment() throws Exception {
        MimeMessage mimeMessage = new MimeMessage(Session.getDefaultInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.sendBadgeEmail(
                "amal@test.com",
                "New badge unlocked: Expert",
                "Email body",
                "pdf-content".getBytes(),
                "certificate.pdf"
        );

        verify(mailSender).send(mimeMessage);
        mimeMessage.saveChanges();
        Address[] recipients = mimeMessage.getAllRecipients();
        assertThat(recipients).hasSize(1);
        assertThat(((InternetAddress) recipients[0]).getAddress()).isEqualTo("amal@test.com");
        assertThat(((InternetAddress) mimeMessage.getFrom()[0]).getAddress()).isEqualTo("no-reply@freelink.local");
        assertThat(mimeMessage.getSubject()).isEqualTo("New badge unlocked: Expert");
        assertThat(mimeMessage.getContent()).isInstanceOf(Multipart.class);
    }
}
