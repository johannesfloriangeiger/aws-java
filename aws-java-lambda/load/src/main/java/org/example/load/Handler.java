package org.example.load;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;

public record Handler(DynamoDBFacade dynamoDBFacade) implements RequestHandler<SQSEvent, Handler.Response> {

    private static final String FILE_NAME = "fileName";

    public record Response(String message) {
    }

    /**
     * AWS Lambda default constructor.
     */
    @SuppressWarnings("unused")
    public Handler() {
        this(new DynamoDBFacade());
    }

    @Override
    public Response handleRequest(final SQSEvent sqsEvent, final Context context) {
        sqsEvent.getRecords().forEach(this::process);

        return new Response("Processed %d entries".formatted(sqsEvent.getRecords().size()));
    }

    private void process(final SQSEvent.SQSMessage sqsMessage) {
        final var messageAttributes = sqsMessage.getMessageAttributes();
        final var messageAttribute = messageAttributes.get(FILE_NAME);
        final var fileName = messageAttribute.getStringValue();
        final var content = sqsMessage.getBody();
        this.dynamoDBFacade.persist(fileName, content);
    }
}