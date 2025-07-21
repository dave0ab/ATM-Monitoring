package com.zemenbank.atmMonitoring.controller.v1.atmMonitor;


import com.zemenbank.atmMonitoring.controller.BaseController;
import com.zemenbank.atmMonitoring.services.MailService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Mail REST APIs", description = "REST APIs for Sending Emails")
@RestController
@RequestMapping("/v1/mail")
@RequiredArgsConstructor
public class MailController extends BaseController {

    private final MailService mailService;

    @PostMapping("/send")
    public String sendMail(@RequestParam String subject,
                           @RequestParam String message,
                           @RequestParam String to) {
        mailService.sendMail(subject, message, to);
        return "Email sent successfully to " + to;
    }
}
