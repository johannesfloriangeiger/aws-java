package org.example.load;


import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class HandlerTest {

    private final DynamoDBFacade dynamoDBFacade = Mockito.mock(DynamoDBFacade.class);

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(this.dynamoDBFacade);
    }

    @Test
    void creates() {
        new Handler();
    }

    @Test
    void returnsCount() {
        final var sqsEvent = new SQSEvent();
        final var sqsMessage = new SQSEvent.SQSMessage();
        final var fileNameMessageAttribute = new SQSEvent.MessageAttribute();
        fileNameMessageAttribute.setStringValue("key");
        sqsMessage.setMessageAttributes(Map.ofEntries(Map.entry("fileName", fileNameMessageAttribute)));
        sqsMessage.setBody("content");
        sqsEvent.setRecords(List.of(sqsMessage));

        assertEquals("Processed 1 entries", new Handler(this.dynamoDBFacade)
                .handleRequest(sqsEvent, null).message());

        Mockito.verify(this.dynamoDBFacade)
                .persist("key", "content");
    }
}