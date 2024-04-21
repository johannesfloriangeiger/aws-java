package org.example.transform;


import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class HandlerTest {

    private final SQSFacade sqsFacadeMock = Mockito.mock(SQSFacade.class);

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(this.sqsFacadeMock);
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
        final var fileSizeMessageAttribute = new SQSEvent.MessageAttribute();
        fileSizeMessageAttribute.setStringValue("3");
        sqsMessage.setMessageAttributes(Map.ofEntries(Map.entry("fileName", fileNameMessageAttribute), Map.entry("fileSize", fileSizeMessageAttribute)));
        sqsMessage.setBody("content");
        sqsEvent.setRecords(List.of(sqsMessage));

        assertEquals("Processed 1 entries", new Handler(this.sqsFacadeMock)
                .handleRequest(sqsEvent, null).message());

        Mockito.verify(this.sqsFacadeMock)
                .send("key", "content");
    }
}