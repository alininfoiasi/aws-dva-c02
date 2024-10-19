#!/bin/bash
yum update -y
yum install -y httpd
systemctl start httpd
systemctl enable httpd
EC2_AVAILABILITY_ZONE=$(curl -s http://169.254.169.254/latest/meta-data/placement/availability-zone)
echo "<h1>Hello $(hostname -f), welcome to AZ $EC2_AVAILABILITY_ZONE </h1>" > /var/www/html/index.html
