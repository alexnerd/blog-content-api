# blog-content-api

blog-content-api is a part of personal blog engine. Service reads json files from storage and send them to render service.

# Build
mvn clean package && docker build -t com.alexnerd/blog-content-api .

# RUN

docker rm -f blog-content-api || true && docker run -d -p 8080:8080 -p 4848:4848 --name blog-content-api com.alexnerd/blog-content-api 

# System Test

Switch to the "-st" module and perform:

mvn compile failsafe:integration-test