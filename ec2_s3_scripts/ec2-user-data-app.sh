#!/bin/bash
yum update -y
yum install -y java-17-amazon-corretto-headless

mkdir app
chown -R ec2-user:ec2-user /app
chmod 755 /app

cd /app
aws s3 cp s3://dvac02-bucket1/ec2-rest-api-0.0.1-SNAPSHOT.jar ec2-rest-api-0.0.1-SNAPSHOT.jar

java -jar ec2-rest-api-0.0.1-SNAPSHOT.jar
