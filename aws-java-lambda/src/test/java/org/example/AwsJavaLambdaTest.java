package org.example;

import org.junit.jupiter.api.Test;
import software.amazon.awscdk.App;
import software.amazon.awscdk.assertions.Template;

import java.util.List;
import java.util.Map;

public class AwsJavaLambdaTest {

    @Test
    public void testStack() {
        App app = new App();
        AwsJavaLambdaStack stack = new AwsJavaLambdaStack(app, "test");

        Template template = Template.fromStack(stack);

        template.hasResourceProperties("AWS::S3::Bucket", Map.ofEntries(
                Map.entry("BucketName", Map.entry("Fn::Join", List.of("", List.of(Map.entry("Ref", "AWS::AccountId"), "-extract"))))
        ));

        template.hasResourceProperties("AWS::Lambda::Function", Map.ofEntries(
                Map.entry("FunctionName", "Extract"),
                Map.entry("Handler", "org.example.extract.Handler::handleRequest"),
                Map.entry("Runtime", "java21"),
                Map.entry("Environment", Map.entry("Variables", Map.entry("QUEUE_URL", stack.resolve(stack.getTransformQueue().getQueueUrl())))),
                Map.entry("Timeout", 120)
        ));

        template.hasResourceProperties("AWS::SQS::Queue", Map.ofEntries(
                Map.entry("QueueName", "Load"),
                Map.entry("VisibilityTimeout", 120)
        ));

        template.hasResourceProperties("AWS::Lambda::Function", Map.ofEntries(
                Map.entry("FunctionName", "Transform"),
                Map.entry("Handler", "org.example.transform.Handler::handleRequest"),
                Map.entry("Runtime", "java21"),
                Map.entry("Environment", Map.entry("Variables", Map.entry("QUEUE_URL", stack.resolve(stack.getLoadQueue().getQueueUrl())))),
                Map.entry("Timeout", 120)
        ));

        template.hasResourceProperties("AWS::DynamoDB::Table", Map.ofEntries(
                Map.entry("TableName", "files")
        ));

        template.hasResourceProperties("AWS::Lambda::Function", Map.ofEntries(
                Map.entry("FunctionName", "Load"),
                Map.entry("Handler", "org.example.load.Handler::handleRequest"),
                Map.entry("Runtime", "java21"),
                Map.entry("Environment", Map.entry("Variables", Map.entry("TABLE_NAME", stack.resolve(stack.getTable().getTableName())))),
                Map.entry("Timeout", 120)
        ));
    }
}
