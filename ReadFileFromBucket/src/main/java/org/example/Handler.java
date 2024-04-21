package org.example;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.io.IOException;

public class Handler implements RequestHandler<Handler.Input, Handler.Output> {

    public record Input(String bucket, String key) {
    }

    public record Output(String content) {
    }

    @Override
    public Handler.Output handleRequest(final Handler.Input input, final Context context) {
        try (final var s3Client = S3Client.builder()
                .build()) {
            final var getObjectRequest = GetObjectRequest.builder()
                    .bucket(input.bucket)
                    .key(input.key)
                    .build();
            final var responseInputStream = s3Client.getObject(getObjectRequest);
            final var bytes = responseInputStream.readAllBytes();
            final var content = new String(bytes);

            return new Handler.Output(content);
        } catch (final IOException runtimeException) {
            throw new RuntimeException(runtimeException);
        }
    }
}