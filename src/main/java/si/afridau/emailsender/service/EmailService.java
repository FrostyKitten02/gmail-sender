package si.afridau.emailsender.service;

import jakarta.mail.Message;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import si.afridau.emailsender.model.EmailAttempt;

import java.util.ArrayList;
import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;

    @Value("${spring.mail.from}")
    private String from;

    private static final int ATTEMPT_INTERVAL_MILLIS = 10 * 60 * 1000;
    private static final int MAX_ATTEMPTS = 5;

    private final List<EmailAttempt> emailAttempts = new ArrayList<>();

    @Async
    public void sendSimpleMail(String to, String content, String subject) {
        MimeMessage message = createMimeMessage(to, subject, content);
        sendEmail(message);
    }

    private MimeMessage createMimeMessage(String to, String subject, String template) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            message.setFrom(new InternetAddress(from));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
            message.setSubject(subject);
            message.setContent(template, "text/html");
            return message;
        } catch (Exception e) {
            log.error("Failed to create email message!");
            log.error(e.getLocalizedMessage(), e);
            return null;
        }
    }

    private void sendEmail(MimeMessage ...messages) {
        try {
            sendEmailInternal(messages);
        } catch (Exception e) {
            log.error("Failed to send email!");
            log.error(e.getLocalizedMessage(), e);

            for (MimeMessage message : messages) {
                EmailAttempt attemp = new EmailAttempt();
                attemp.setMessage(message);
                attemp.setLastAttempt(System.currentTimeMillis());
                emailAttempts.add(attemp);
            }
        }
    }

    private void sendEmailInternal(MimeMessage ...messages) throws MailException {
        mailSender.send(messages);
    }

    private void retryEmail(EmailAttempt attempt) {
        try {
            sendEmailInternal(attempt.getMessage());
            emailAttempts.remove(attempt);
        } catch (Exception e) {
            log.error("Failed to send email!");
            log.error(e.getLocalizedMessage(), e);
            attempt.setLastAttempt(System.currentTimeMillis());
            attempt.setAttempt(attempt.getAttempt() + 1);
        }
    }

    @Scheduled(fixedRate = ATTEMPT_INTERVAL_MILLIS / 2)
    private void retryFailedEmails() {
        List<EmailAttempt> toRemove = new ArrayList<>();
        for (EmailAttempt attempt : emailAttempts) {
            if (attempt.getAttempt() >= MAX_ATTEMPTS) {
                toRemove.add(attempt);
            }

            if (System.currentTimeMillis() - attempt.getLastAttempt() > ATTEMPT_INTERVAL_MILLIS) {
                retryEmail(attempt);
            }
        }

        for (EmailAttempt remove : toRemove) {
            try {
                log.info("Removing email from queue after {} attempts content {}", MAX_ATTEMPTS, remove.getMessage().getContent().toString());
            } catch (Exception e) {
                log.info("Removing email from queue after {} attempts. Failed to get content.", MAX_ATTEMPTS);
            }
            emailAttempts.remove(remove);
        }
    }

}
