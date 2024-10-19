import json
import boto3
import os

print('Loading function v1.11')

client = boto3.client('sns')

def lambda_handler(event, context):
    print("Received event: " + json.dumps(event, indent=2), end='\n')
    topicArn = os.environ['SNSARN']
    print("SNS arn: " + topicArn)
    for message in event['Records']:
        response = client.publish(TopicArn=topicArn, Message=message['body'])
    print("Message published to SNS: " + message['body'])
    return (response)
