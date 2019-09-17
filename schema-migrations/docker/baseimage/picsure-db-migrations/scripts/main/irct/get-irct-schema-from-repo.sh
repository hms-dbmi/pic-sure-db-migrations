#!/bin/bash

echo "IRCT Schema From Repo -- Fetching: Begin"

echo $pwd

cd /picsure-db-migrations/migrations/main/irct 
mkdir irct_temp
cd irct_temp

git clone $1 .
git checkout $2
git status -u
git branch

echo $pwd

cd IRCT-API/src/main/resources/db/sql
ls -altr


cp *.sql /picsure-db-migrations/migrations/main/irct

cd /picsure-db-migrations/migrations/main/irct

rm -rf irct_temp


echo "IRCT Schema From Repo -- Fetching: End"