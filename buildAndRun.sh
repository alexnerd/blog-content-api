#!/bin/sh
mvn clean package && docker build -t com.alexnerd/blog-content-api .
docker rm -f blog-content-api || true && docker run -d -p 8080:8080 -p 4848:4848 --name blog-content-api com.alexnerd/blog-content-api
