#!/bin/bash

echo "IRCT (custom) Schema From Repo -- Fetching: Begin"

echo $pwd

cd /picsure-db-migrations/migrations/custom/irct 
mkdir irct_temp
cd irct_temp

git clone $1 .
git checkout $2
git status -u
git branch

echo $pwd

cd irct
ls -altr


cp *.sql /picsure-db-migrations/migrations/custom/irct

cd /picsure-db-migrations/migrations/custom/irct

rm -rf irct_temp


echo "IRCT (custom) Schema From Repo -- Fetching: End"