FROM braindoctor/clustercode:builder as builder

COPY / .

RUN \
    sh ./gradlew shadowJar && \
    tree

FROM openjdk:8-jdk-alpine

ARG SRC_DIR="/usr/local/src/clustercode"
ARG TGT_DIR="/opt/clustercode"
#USER root

WORKDIR ${TGT_DIR}

ENV \
    CC_DEFAULT_DIR="${TGT_DIR}/default" \
    CC_CONFIG_DIR="${TGT_DIR}/config" \
    CC_LOG_CONFIG_FILE="default/config/log4j2.xml" \
    JAVA_ARGS="" \
    CC_CLUSTER_JGROUPS_BIND_PORT=7600

VOLUME \
    /input \
    /output \
    /profiles \
    /var/tmp/clustercode \
    $CC_CONFIG_DIR

EXPOSE \
    $CC_CLUSTER_JGROUPS_BIND_PORT/tcp \
    $CC_CLUSTER_JGROUPS_BIND_PORT/udp

CMD ["/opt/clustercode/docker-entrypoint.sh"]

COPY docker/docker-entrypoint.sh ${TGT_DIR}/docker-entrypoint.sh
RUN \
    apk update && \
    apk upgrade && \
    apk add --no-cache ffmpeg nano curl

COPY --from=builder ${SRC_DIR}/build/libs/clustercode.jar ${TGT_DIR}/

