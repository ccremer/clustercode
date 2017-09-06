FROM openjdk:8-jdk-alpine

USER root

WORKDIR /usr/src/clustercode

ENV \
    CC_DEFAULT_DIR="/usr/src/clustercode/default" \
    CC_CONFIG_FILE="/usr/src/clustercode/config/clustercode.properties" \
    CC_CONFIG_DIR="/usr/src/clustercode/config" \
    CC_LOG_CONFIG_FILE="default/config/log4j2.xml" \
    JAVA_ARGS=""

ARG \
    GRADLE_VERSION="4.0.1"

VOLUME \
    /input \
    /output \
    /profiles \
    /var/tmp/clustercode \
    $CC_CONFIG_DIR

EXPOSE \
    7600/tcp 7600/udp \
    5005 \
    8080

CMD ["/usr/src/clustercode/docker-entrypoint.sh"]

RUN \
    mkdir /run/nginx && \
    apk update && \
    apk upgrade && \
    apk add --no-cache ffmpeg nginx supervisor nano curl openssl unzip nodejs-current-npm

COPY \
    build.gradle \
    settings.gradle \
    package.json \
    .babelrc \
    .postcssrc.js \
    docker/docker-entrypoint.sh docker/supervisord.conf ./

COPY docker/nginx.conf /etc/nginx/nginx.conf
RUN \
    echo "Installing Gradle" && \
    wget -O gradle.zip "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip" && \
    unzip -q gradle.zip && \
    rm gradle.zip && \
    gradle-${GRADLE_VERSION}/bin/gradle downloadDependencies && \
    echo "Installing node packages" && \
    npm install --silent --no-optional

COPY webpack webpack
COPY webpackcfg webpackcfg
COPY docker/default default
COPY static static
COPY src src

RUN \
    echo "Building clustercode" && \
    gradle-${GRADLE_VERSION}/bin/gradle fullBuild && \
    mv build/libs/clustercode.jar clustercode.jar && \
    rm -r build
RUN \
    echo "Bulding clustercode-admin" && \
    npm run build
