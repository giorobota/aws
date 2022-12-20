package org.example;

import com.amazonaws.services.lambda.AWSLambdaAsyncClient;
import com.amazonaws.services.lambda.model.GetAccountSettingsRequest;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Handler implements RequestHandler<SQSEvent, String> {
    private static final Logger logger = LoggerFactory.getLogger(Handler.class);
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final AWSLambdaAsyncClient lambdaClient = new AWSLambdaAsyncClient();

    public Handler() {
        CompletableFuture<GetAccountSettingsResponse> accountSettings = lambdaClient.getAccountSettings(new GetAccountSettingsRequest());
        try {
            GetAccountSettingsResponse settings = accountSettings.get();
        } catch (Exception e) {
            e.getStackTrace();
        }
    }

    @Override
    public String handleRequest(SQSEvent event, Context context) {
        String response = new String();
        logger.info("Getting account settings");
        CompletableFuture<GetAccountSettingsResponse> accountSettings =
                lambdaClient.getAccountSettings(GetAccountSettingsRequest.builder().build());
        // log execution details
        logger.info("ENVIRONMENT VARIABLES: {}", gson.toJson(System.getenv()));
        logger.info("CONTEXT: {}", gson.toJson(context));
        logger.info("EVENT: {}", gson.toJson(event));
        // process event
        for (SQSMessage msg : event.getRecords()) {
            logger.info(msg.getBody());
        }
        try {
            GetAccountSettingsResponse settings = accountSettings.get();
            response = gson.toJson(settings.accountUsage());
            logger.info("Account usage: {}", response);
        } catch (Exception e) {
            e.getStackTrace();
        }
        return response;
    }
}