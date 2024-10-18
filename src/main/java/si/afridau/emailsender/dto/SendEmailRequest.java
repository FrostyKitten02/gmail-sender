package si.afridau.emailsender.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendEmailRequest {
    private String recipient;
    private String subject;
    private String content;
}
