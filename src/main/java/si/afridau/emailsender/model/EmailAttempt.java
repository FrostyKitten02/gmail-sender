package si.afridau.emailsender.model;

import jakarta.mail.internet.MimeMessage;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailAttempt {
    private MimeMessage message;
    private long lastAttempt;
    private int attempt;
}
