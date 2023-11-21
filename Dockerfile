FROM --platform=linux/arm64/v8 alexnerd/payara-micro:6
ENV ARCHIVE_NAME content.war
COPY ./target/content.war ${DEPLOYMENT_DIR}
