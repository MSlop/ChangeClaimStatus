package com.example.Change.Payment.Status;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.EnableZeebeClient;
import io.camunda.zeebe.spring.client.annotation.ZeebeWorker;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootApplication
@EnableZeebeClient
public class SendEmail {

    @Autowired
    private JavaMailSender javaMailSender;

    public static void main(String[] args) {
        SpringApplication.run(SendEmail.class, args);
    }

    @ZeebeWorker(type = "send_email")
    public void SendEmail(final JobClient client, final ActivatedJob job) {

        Map<String, Object> variablesAsMap = job.getVariablesAsMap();

        //String sender = variablesAsMap.get("sender").toString();
        String receiver = variablesAsMap.get("managerEmail").toString();
        //String subject = variablesAsMap.get("subject").toString();
        //String body = variablesAsMap.get("body").toString();

        String policyId = variablesAsMap.get("policyId").toString();
        String claimId = variablesAsMap.get("claimId").toString();

        String sender = "mslop2017@gmail.com";
        //String receiver = "mslop2017@gmail.com";
        String subject = "Test email";
        String body = "You should appove changing status for Policy = " + policyId + ", Claim = " + claimId;

        List<String> invalidEmailAddresses = new ArrayList();
        boolean invalidEmails = false;

        if(!ValidateEmail.isValidEmail(sender)){
            invalidEmailAddresses.add(sender);
            invalidEmails = true;
        }
        if(!ValidateEmail.isValidEmail(receiver)){
            invalidEmailAddresses.add(receiver);
            invalidEmails = true;
        }
        if(invalidEmails)
        {
            client.newThrowErrorCommand(job)
                    .errorCode("INVALID_EMAIL")
                    .send();
        }else {

            try {
                sendMail(sender, receiver, subject, body);
                String resultMessage = "Mail Sent Successfully to " + receiver;

                HashMap<String, Object> variables = new HashMap<>();
                variables.put("result", resultMessage);
                client.newCompleteCommand(job.getKey())
                        .variables(variables)
                        .send()
                        .exceptionally((throwable -> {
                            throw new RuntimeException("Could not complete job", throwable);
                        }));
            } catch (MessagingException e) {
                e.printStackTrace();
                client.newFailCommand(job.getKey());
            }
        }
    }

    private void sendMail(String sender, String receiver, String subject, String body) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setFrom(sender);
        helper.setTo(receiver);
        helper.setSubject(subject);
        helper.setText(body, true);

        javaMailSender.send(message);
    }

}
