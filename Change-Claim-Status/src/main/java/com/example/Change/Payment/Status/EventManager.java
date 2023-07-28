package com.example.Change.Payment.Status;

import io.camunda.zeebe.client.ZeebeClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class EventManager implements CommandLineRunner {

    public static void main(String[] args){
        SpringApplication.run(EventManager.class, args);
    }

    @Autowired
    private ZeebeClient client;

    @Override
    public void run(String... args) throws Exception{

        final Map<String, Object> variables = new HashMap<String, Object>();

        String claimId = "0001";

        // Fill data
        variables.put("managerEmail","mslop2017@gmail.com");
        variables.put("policyId","100");
        variables.put("claimId", claimId);
        variables.put("currentStatus", "open");

        // Send event messageName = "CreateClaim" with variables = variables
        String messageKey = claimId;
        client.newPublishMessageCommand().messageName("change_status").correlationKey(messageKey).variables(variables)
                .send()
                .exceptionally( throwable -> { throw new RuntimeException( "Not message", throwable); } );
    }
}
