package org.example.extract;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class HandlerTest {

    private final S3Facade s3FacadeMock = mock(S3Facade.class);

    private final SQSFacade sqsFacadeMock = mock(SQSFacade.class);

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(this.s3FacadeMock, this.sqsFacadeMock);
    }

    @Test
    void creates() {
        new Handler();
    }

    @Test
    void returnsMessage() {
        when(this.s3FacadeMock.getContent("bucket", "key"))
                .thenReturn("content");

        assertEquals("Processed file s3://bucket/key", new Handler(this.s3FacadeMock, this.sqsFacadeMock)
                .handleRequest(new Handler.Request("bucket", "key"), null).message());

        verify(this.s3FacadeMock)
                .getContent("bucket", "key");
        verify(this.sqsFacadeMock)
                .send("key", "content");
    }
}