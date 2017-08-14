FROM maven:3-jdk-8-alpine

# Run the cachable commands first
WORKDIR /usr/src/clustercode

ENV \
    CC_DEFAULT_DIR="/usr/src/clustercode/default" \
    CC_CONFIG_FILE="/usr/src/clustercode/config/clustercode.properties" \
    CC_CONFIG_DIR="/usr/src/clustercode/config" \
    CC_LOG_CONFIG_FILE="default/log4j2.xml" \
    CC_MEDIA_INPUT_DIR="/input" \
    CC_MEDIA_OUTPUT_DIR="/output"

VOLUME \
    $CC_MEDIA_INPUT_DIR \
    $CC_MEDIA_OUTPUT_DIR \
    $CC_CONFIG_DIR

EXPOSE \
    7600/tcp 7600/udp

CMD ["/usr/src/clustercode/docker-entrypoint.sh"]

RUN \
    apk add --no-cache \
        ffmpeg \
        x265 \
        nano


COPY pom.xml lombok.config docker ./
COPY src src/

RUN \
    mvn package -P package -e -B && \
    mv target/clustercode-jar-with-dependencies.jar clustercode.jar && \
    rm -r src && \
    rm -r target && \
    rm lombok.config && \
    rm pom.xml