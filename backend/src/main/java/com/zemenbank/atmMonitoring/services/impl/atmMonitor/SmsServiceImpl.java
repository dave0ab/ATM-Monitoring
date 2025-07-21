package com.zemenbank.atmMonitoring.services.impl.atmMonitor;

import com.zemenbank.atmMonitoring.services.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class SmsServiceImpl implements SmsService {

    @Value("${sms.username}")
    private String username;

    @Value("${sms.password}")
    private String password;

    @Value("${sms.from}")
    private String from;

    // A default message from configuration, if no message is provided at runtime.
    @Value("${sms.message}")
    private String defaultMessage;

    @Value("${sms.gateway}")
    private String url;

    /**
     * Sends an SMS using the provided message and phone number.
     * If messageString is null or empty, then the defaultMessage (from configuration) is used.
     *
     * @param messageString the SMS message content
     * @param phoneNumber   the recipient phone number
     */
    @Override
    public void sendSms(String messageString, String phoneNumber) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(username, password);

        // Build the JSON payload using the method parameters (avoiding hard coding)
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("to", phoneNumber);   // Use the parameter instead of a hard-coded value
        jsonObject.put("from", from);          // Using the configured "from" value

        // Use the provided message if available, otherwise fall back to the default message.
        String smsContent = (messageString != null && !messageString.isEmpty())
                ? messageString
                : defaultMessage;
        jsonObject.put("content", smsContent);
        jsonObject.put("coding", 8);

        String payload = jsonObject.toString();
        log.info("SMS Payload: {}", payload);

        String endpoint = url + "secure/send";
        log.info("Sending SMS using endpoint: {}", endpoint);

        HttpEntity<String> httpEntity = new HttpEntity<>(payload, headers);
        ResponseEntity<String> response = restTemplate.exchange(
                endpoint,
                HttpMethod.POST,
                httpEntity,
                String.class
        );

        log.info("Response Status: {}", response.getStatusCode());
        if (response.getStatusCode() == HttpStatus.OK) {
            log.info("SMS sent successfully.");
        } else {
            log.error("SMS failed with status: {}", response.getStatusCode());
        }
    }
}
