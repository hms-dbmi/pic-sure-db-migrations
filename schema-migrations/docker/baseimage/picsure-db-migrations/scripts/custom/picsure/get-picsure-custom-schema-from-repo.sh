#!/bin/bash

echo "PICSURE (custom) Schema From Repo -- Fetching: Begin"

echo $pwd

cd /picsure-db-migrations/migrations/custom/picsure 
mkdir picsure_temp
cd picsure_temp

git clone $1 .
git checkout $2
git status -u
git branch

echo $pwd

cd picsure
ls -altr


cp *.sql /picsure-db-migrations/migrations/custom/picsure

cd /picsure-db-migrations/migrations/custom/picsure

rm -rf picsure_temp


echo "PICSURE (custom) Schema From Repo -- Fetching: End"