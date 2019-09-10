#!/bin/bash

echo "Get code from Repo: Begin"

echo $pwd

mkdir /migrations-temp
chmod -R 777 /migrations-temp

mkdir /migrations
chmod -R 777 /migrations
cd /migrations
mkdir sql
chmod -R 777 /migrations/sql


cd /migrations-temp
git clone $1 .
git checkout $2
git status -u
git branch

cd pic-sure-api-data/src/main/resources/db/

cp -R sql/ /migrations/sql

chmod -R 777 /migrations/sql
 


echo "Get code from Repo: End"