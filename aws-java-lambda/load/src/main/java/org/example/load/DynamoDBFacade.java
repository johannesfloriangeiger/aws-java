package org.example.load;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.util.Map;
import java.util.function.BiFunction;

public class DynamoDBFacade {

    private static final String TABLE_NAME = System.getenv("TABLE_NAME");

    public static final String FILE_NAME = "fileName";

    private static final String CONTENT = "content";

    private static final BiFunction<String, String, PutItemRequest> PUT_ITEM_REQUEST = (fileName, content) -> PutItemRequest.builder()
            .tableName(TABLE_NAME)
            .item(Map.ofEntries(Map.entry(FILE_NAME, AttributeValue.fromS(fileName)),
                    Map.entry(CONTENT, AttributeValue.fromS(content))))
            .build();

    public void persist(final String fileName, final String content) {
        try (final var dynamoDbClient = DynamoDbClient.builder()
                .build()) {
            final var putItemRequest = PUT_ITEM_REQUEST.apply(fileName, content);
            dynamoDbClient.putItem(putItemRequest);
        }
    }
}