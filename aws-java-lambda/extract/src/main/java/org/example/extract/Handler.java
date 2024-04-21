package org.example.extract;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public record Handler(S3Facade s3Facade,
                      SQSFacade sqsFacade) implements RequestHandler<Handler.Request, Handler.Response> {

    public record Request(String bucket, String key) {
    }

    public record Response(String message) {
    }

    /**
     * AWS Lambda default constructor.
     */
    @SuppressWarnings("unused")
    public Handler() {
        this(new S3Facade(), new SQSFacade());
    }

    @Override
    public Response handleRequest(final Request request, final Context context) {
        final var content = this.s3Facade.getContent(request.bucket, request.key);
        this.sqsFacade.send(request.key, content);

        return new Response("Processed file s3://%s/%s".formatted(request.bucket, request.key));
    }
}