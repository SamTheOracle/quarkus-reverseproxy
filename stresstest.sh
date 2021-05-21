#!/bin/bash
path=$PWD

for i in {1..10000000}
do
  echo "Making $i request"
  curl  http://localhost:8085/api/v1/tracks/positions/213
done