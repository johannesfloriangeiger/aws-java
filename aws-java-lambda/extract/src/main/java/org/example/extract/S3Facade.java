package org.example.extract;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.io.IOException;
import java.util.function.BiFunction;

public class S3Facade {

    private static final BiFunction<String, String, GetObjectRequest> GET_OBJECT_REQUEST = (bucket, key) -> GetObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .build();

    public String getContent(final String bucket, final String key) {
        try (final var s3Client = S3Client.builder()
                .build()) {
            final var getObjectRequest = GET_OBJECT_REQUEST.apply(bucket, key);
            final var bytes = s3Client.getObject(getObjectRequest)
                    .readAllBytes();

            return new String(bytes);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
}