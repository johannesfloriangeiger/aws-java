package org.example.transform;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;

public record Handler(SQSFacade sqsFacade) implements RequestHandler<SQSEvent, Handler.Response> {

    public record Response(String message) {
    }

    /**
     * AWS Lambda default constructor.
     */
    @SuppressWarnings("unused")
    public Handler() {
        this(new SQSFacade());
    }

    @Override
    public Response handleRequest(final SQSEvent sqsEvent, final Context context) {
        sqsEvent.getRecords().forEach(this::process);

        return new Response("Processed %d entries".formatted(sqsEvent.getRecords().size()));
    }

    private void process(final SQSEvent.SQSMessage sqsMessage) {
        final var messageAttributes = sqsMessage.getMessageAttributes();
        final var messageAttribute = messageAttributes.get(SQSFacade.FILE_NAME);
        final var fileName = messageAttribute.getStringValue();
        final var content = sqsMessage.getBody();
        this.sqsFacade.send(fileName, content);
    }
}