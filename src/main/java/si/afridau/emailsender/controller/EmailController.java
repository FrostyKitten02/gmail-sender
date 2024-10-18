package si.afridau.emailsender.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import si.afridau.emailsender.dto.SendEmailRequest;
import si.afridau.emailsender.service.EmailService;

@CrossOrigin
@RestController
@RequiredArgsConstructor
@RequestMapping("/email")
public class EmailController {

    private final EmailService emailService;
    @PostMapping("/send")
    public void sendEmail(
            @RequestBody SendEmailRequest request
    ) {
        emailService.sendSimpleMail(request.getRecipient(), request.getContent(), request.getSubject());
    }


}
