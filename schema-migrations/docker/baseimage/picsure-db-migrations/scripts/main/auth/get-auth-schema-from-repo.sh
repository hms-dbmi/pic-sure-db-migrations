#!/bin/bash

echo "Auth Schema From Repo -- Fetching: Begin"

echo $pwd

cd /picsure-db-migrations/migrations/main/auth 
mkdir auth_temp
cd auth_temp

git clone $1 .
git checkout $2
git status -u
git branch

echo $pwd

cd pic-sure-auth-db/db/sql
ls -altr


cp *.sql /picsure-db-migrations/migrations/main/auth

cd /picsure-db-migrations/migrations/main/auth

rm -rf auth_temp


echo "Auth Schema From Repo -- Fetching: End"