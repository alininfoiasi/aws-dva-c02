#!/bin/bash
yum update -y
yum install -y httpd
systemctl start httpd
systemctl enable httpd
usermod -a -G apache ec2-user
chown -R ec2-user:apache /var/www
chmod 2775 /var/www
find /var/www -type d -exec chmod 2775 {} \;
find /var/www -type f -exec chmod 0664 {} \;

#EC2_INSTANCE_ID=$(ec2metadata --instance-id | cut -d ":" -f 2 | xargs)
TOKEN=`curl -s -X PUT "http://169.254.169.254/latest/api/token" -H "X-aws-ec2-metadata-token-ttl-seconds: 600"`
# Using that token, get the AZ.  Note the use of back ticks to call curl, and the variable name
EC2_INSTANCE_AZ=`curl -s -H "X-aws-ec2-metadata-token: $TOKEN" http://169.254.169.254/latest/meta-data/placement/availability-zone`
# And using the token, get the instance ID
EC2_INSTANCE_ID=`curl -s -H "X-aws-ec2-metadata-token: $TOKEN" http://169.254.169.254/latest/meta-data/instance-id`

aws ec2 associate-iam-instance-profile \
    --instance-id $EC2_INSTANCE_ID \
    --iam-instance-profile Name="ReadAccessRoleS3"
aws s3 cp --recursive s3://dvac02-bucket1/website /var/www/html
