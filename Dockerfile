FROM maven:3-jdk-8-alpine

WORKDIR /usr/src/clustercode

ENV \
    CC_DEFAULT_DIR="/usr/src/clustercode/default" \
    CC_CONFIG_FILE="/usr/src/clustercode/config/clustercode.properties" \
    CC_CONFIG_DIR="/usr/src/clustercode/config" \
    CC_LOG_CONFIG_FILE="default/config/log4j2.xml"

VOLUME \
    /input \
    /output \
    /profiles \
    /var/tmp/clustercode \
    $CC_CONFIG_DIR

# Port 5005 is used for java remote debug, do not publish this port in production.
EXPOSE \
    7600/tcp 7600/udp \
    5005

CMD ["/usr/src/clustercode/docker-entrypoint.sh"]

RUN \
    apk update && \
    apk upgrade && \
    apk add --no-cache ffmpeg

COPY pom.xml docker ./
COPY src src/

RUN \
    mvn package -P package -e -B \
        -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn && \
    mv target/clustercode-jar-with-dependencies.jar clustercode.jar && \
    rm -r src && \
    rm -r target