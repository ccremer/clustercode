#______________________________________________________________________________
#### Base Image, to save build time on local dev machine
ARG ARCH
FROM multiarch/alpine:${ARCH} as base

ENTRYPOINT ["/bin/bash"]

ARG SRC_DIR="/usr/local/src/clustercode"
ARG TGT_DIR="/opt/clustercode"

WORKDIR ${TGT_DIR}

RUN \
    apk update && \
    apk upgrade && \
    apk add --no-cache openjdk8-jre ffmpeg nano curl bash

COPY docker/docker-entrypoint.sh ${TGT_DIR}/docker-entrypoint.sh
COPY docker/default ${TGT_DIR}/default/

#______________________________________________________________________________
#### Builder Image
FROM braindoctor/clustercode:builder as builder

COPY / .

RUN \
    sh ./gradlew shadowJar

#______________________________________________________________________________
#### Runtime Image
FROM base

ARG SRC_DIR="/usr/local/src/clustercode"
ARG TGT_DIR="/opt/clustercode"

WORKDIR ${TGT_DIR}

ENV \
    CC_DEFAULT_DIR="${TGT_DIR}/default" \
    CC_CONFIG_DIR="${TGT_DIR}/config" \
    CC_LOG_CONFIG_FILE="default/config/log4j2.xml" \
    JAVA_ARGS="" \
    CC_CLUSTER_JGROUPS_BIND_PORT=7600

RUN \
    # Let's create the directories first so we can apply the permissions:
    mkdir -m 664 /input /output /profiles /var/tmp/clustercode ${CC_CONFIG_DIR}

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

COPY --from=builder ${SRC_DIR}/build/libs/clustercode.jar ${TGT_DIR}/
