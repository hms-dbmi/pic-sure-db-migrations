#!/bin/bash

echo "PICSURE Schema From Repo -- Fetching: Begin"

echo $pwd

cd /picsure-db-migrations/migrations/main/picsure 
mkdir picsure_temp
cd picsure_temp

git clone $1 .
git checkout $2
git status -u
git branch

echo $pwd

cd pic-sure-api-data/src/main/resources/db/sql
ls -altr


cp *.sql /picsure-db-migrations/migrations/main/picsure

cd /picsure-db-migrations/migrations/main/picsure

rm -rf picsure_temp


echo "PICSURE Schema From Repo -- Fetching: End"