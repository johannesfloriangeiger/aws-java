package org.example;

import software.amazon.awscdk.Duration;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.dynamodb.Attribute;
import software.amazon.awscdk.services.dynamodb.AttributeType;
import software.amazon.awscdk.services.dynamodb.Table;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.lambda.eventsources.SqsEventSource;
import software.amazon.awscdk.services.s3.Bucket;
import software.amazon.awscdk.services.sqs.Queue;
import software.constructs.Construct;

import java.util.Map;

public class AwsJavaLambdaStack extends Stack {

    private final Queue transformQueue;

    private final Queue loadQueue;

    private final Table table;

    public AwsJavaLambdaStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public AwsJavaLambdaStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        final var bucket = Bucket.Builder.create(this, "Bucket")
                .bucketName("%s-extract".formatted(this.getAccount()))
                .build();

        this.transformQueue = Queue.Builder.create(this, "TransformQueue")
                .queueName("Transform")
                .visibilityTimeout(Duration.minutes(2))
                .build();
        final var transformQueueEventSource = SqsEventSource.Builder.create(this.transformQueue)
                .build();

        final var extractFunction = Function.Builder.create(this, "ExtractLambda")
                .functionName("Extract")
                .code(Code.fromAsset("./extract/target/extract-1.0-SNAPSHOT.jar"))
                .handler("org.example.extract.Handler::handleRequest")
                .runtime(Runtime.JAVA_21)
                .environment(Map.ofEntries(Map.entry("QUEUE_URL", this.transformQueue.getQueueUrl())))
                .timeout(Duration.minutes(2))
                .build();
        bucket.grantRead(extractFunction);
        this.transformQueue.grantSendMessages(extractFunction);

        this.loadQueue = Queue.Builder.create(this, "LoadQueue")
                .queueName("Load")
                .visibilityTimeout(Duration.minutes(2))
                .build();
        final var loadQueueEventSource = SqsEventSource.Builder.create(this.loadQueue)
                .build();

        final var transformFunction = Function.Builder.create(this, "TransformLambda")
                .functionName("Transform")
                .code(Code.fromAsset("./transform/target/transform-1.0-SNAPSHOT.jar"))
                .handler("org.example.transform.Handler::handleRequest")
                .runtime(Runtime.JAVA_21)
                .environment(Map.ofEntries(Map.entry("QUEUE_URL", this.loadQueue.getQueueUrl())))
                .timeout(Duration.minutes(2))
                .build();
        this.transformQueue.grantConsumeMessages(transformFunction);
        this.loadQueue.grantSendMessages(transformFunction);
        transformFunction.addEventSource(transformQueueEventSource);

        this.table = Table.Builder.create(this, "Table")
                .tableName("files")
                .partitionKey(Attribute.builder()
                        .name("fileName")
                        .type(AttributeType.STRING)
                        .build())
                .build();

        final var loadFunction = Function.Builder.create(this, "LoadLambda")
                .functionName("Load")
                .code(Code.fromAsset("./load/target/load-1.0-SNAPSHOT.jar"))
                .handler("org.example.load.Handler::handleRequest")
                .runtime(Runtime.JAVA_21)
                .environment(Map.ofEntries(Map.entry("TABLE_NAME", this.table.getTableName())))
                .timeout(Duration.minutes(2))
                .build();
        this.loadQueue.grantConsumeMessages(loadFunction);
        this.table.grantWriteData(loadFunction);
        loadFunction.addEventSource(loadQueueEventSource);
    }

    public Queue getTransformQueue() {
        return this.transformQueue;
    }

    public Queue getLoadQueue() {
        return this.loadQueue;
    }

    public Table getTable() {
        return this.table;
    }
}