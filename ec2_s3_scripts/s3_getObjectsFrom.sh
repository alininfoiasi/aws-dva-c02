#!/bin/bash

#RESULT=$(aws s3api list-objects --bucket dva-c02-bucket2 --prefix versioned_files/ --query #'Contents[?LastModified > #`2024-07-15T16:30:00+00:00`]')

#RESULT=$(aws s3api get-object --bucket dva-c02-bucket2 --key versioned_files/file\ a.txt --query 'Contents[?LastModified < #`2024-07-15T23:57:00.000Z`]' code/s3/downloads/file_a.txt)

#RESULT1=$(aws s3api list-objects-v2 --bucket dva-c02-bucket2 --query 'Contents[?LastModified<`2024-07-15T16:57:00+02:00`]')

#RESULT1=$(aws s3api get-object --bucket dva-c02-bucket2 --key versioned_files/file\ a.txt --query 'Contents[?#LastModified<"2024-07-15T16:57:00+02:00"]' code/s3/downloads/file_a.txt)

#RESULT=$(aws s3api list-object-versions --bucket dva-c02-bucket2 --prefix versioned_files/file\ a.txt --query 'Contents[?#LastModified<"2024-07-15T16:57:00+02:00"]' code/s3/downloads/file_a.txt)


RESULT=$(aws s3api list-object-versions --bucket dva-c02-bucket2 --prefix versioned_files/file\ a.txt )

echo "$RESULT"
