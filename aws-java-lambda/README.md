# Java ETL pipeline with S3, SQS and DynamoDB

## Setup

Checkout the code, build it

```
for LAMBDA in extract transform load; do (cd $LAMBDA && mvn clean install); done \
    && mvn clean install
```

and deploy it

```
cdk bootstrap
```

and

```
cdk deploy
```

## Demo

Upload the test file into the Bucket

```
ACCOUNT=$(aws sts get-caller-identity | jq -r .Account)
aws s3 cp ./src/test/resources/file s3://$ACCOUNT-extract
```

trigger the Extract Lambda

```
aws lambda invoke \
    --function-name Extract \
    --payload $(echo '{"bucket":"'$ACCOUNT'-extract","key":"file"}' | base64) out
```

and check DynamoDB

```
aws dynamodb get-item \
    --table-name files \
    --key '{ "fileName": { "S": "file" }}' \
    | jq -r .Item.content.S
```

## Useful commands

 * `mvn package`     compile and run tests
 * `cdk ls`          list all stacks in the app
 * `cdk synth`       emits the synthesized CloudFormation template
 * `cdk deploy`      deploy this stack to your default AWS account/region
 * `cdk diff`        compare deployed stack with current state
 * `cdk docs`        open CDK documentation

Enjoy!
