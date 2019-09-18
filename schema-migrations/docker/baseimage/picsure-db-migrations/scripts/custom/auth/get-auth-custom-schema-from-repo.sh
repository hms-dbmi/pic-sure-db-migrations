#!/bin/bash

echo "Auth (custom) Schema From Repo -- Fetching: Begin"

echo $pwd

cd /picsure-db-migrations/migrations/custom/auth 
mkdir auth_temp
cd auth_temp

echo $1

git clone $1 .
git checkout $2
git status -u
git branch

echo $pwd

cd auth
ls -altr


cp *.sql /picsure-db-migrations/migrations/custom/auth

cd /picsure-db-migrations/migrations/custom/auth

rm -rf auth_temp


echo "Auth (custom) Schema From Repo -- Fetching: End"