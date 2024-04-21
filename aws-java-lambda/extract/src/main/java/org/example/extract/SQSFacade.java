package org.example.extract;

import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.util.Map;
import java.util.function.BiFunction;

public class SQSFacade {

    private static final String QUEUE_URL = System.getenv("QUEUE_URL");

    public static final String FILE_NAME = "fileName";

    public static final String FILE_SIZE = "fileSize";

    private static final BiFunction<String, String, SendMessageRequest> SEND_MESSAGE_REQUEST = (fileName, content) -> SendMessageRequest.builder()
            .queueUrl(QUEUE_URL)
            .messageAttributes(Map.ofEntries(
                    Map.entry(FILE_NAME, MessageAttributeValue.builder()
                            .dataType("String")
                            .stringValue(fileName)
                            .build()),
                    Map.entry(FILE_SIZE, MessageAttributeValue.builder()
                            .dataType("String")
                            .stringValue(String.valueOf(content.length()))
                            .build())))
            .messageBody(content)
            .build();

    public void send(final String fileName, final String content) {
        try (final var sqsClient = SqsClient.builder()
                .build()) {
            final var sendMessageRequest = SEND_MESSAGE_REQUEST.apply(fileName, content);
            sqsClient.sendMessage(sendMessageRequest);
        }
    }
}