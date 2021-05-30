#!/bin/bash

docker build . -t oracolo/proxy:quarkus
docker push oracolo/proxy:quarkus