#!/bin/bash
echo ""
echo "/**** Configure aws begin *****/"
echo ""

/usr/bin/aws configure set aws_access_key_id $AWS_ACCESS_KEY_ID --profile default
/usr/bin/aws configure set aws_secret_access_key $AWS_SECRET_ACCESS_KEY --profile default 
/usr/bin/aws configure set default.region $AWS_REGION --profile default

echo "credentials"
cat ~/.aws/credentials

echo "config"
cat ~/.aws/config


echo ""
echo "/**** Configure aws end*****/"
echo ""