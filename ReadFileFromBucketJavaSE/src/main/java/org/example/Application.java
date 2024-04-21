package org.example;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.io.IOException;

public class Application {

    public static void main(final String[] args) throws IOException {
        try (final var s3Client = S3Client.builder()
                .build()) {
            final var getObjectRequest = GetObjectRequest.builder()
                    .bucket(args[0])
                    .key(args[1])
                    .build();
            final var responseInputStream = s3Client.getObject(getObjectRequest);
            final var bytes = responseInputStream.readAllBytes();
            final var content = new String(bytes);
            System.out.println(content);
        }
    }
}
