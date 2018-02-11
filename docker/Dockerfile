FROM openjdk:8-jdk-alpine
# This is the dockerfile to production usage! Make sure that the build context is the parent directory, not '.' .

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
    8080

CMD ["/usr/src/clustercode/docker-entrypoint.sh"]

COPY . ./

RUN \
    echo "#### Installing alpine packages" && \
    apk update && \
    apk upgrade && \
    apk add --no-cache ffmpeg nginx supervisor openssl tree nodejs-npm unzip && \
    mkdir /run/nginx && \
    echo "#### Installing Gradle" && \
    wget -O gradle.zip "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip" && \
    unzip -q gradle.zip && \
    rm gradle.zip && \
    echo "#### Building clustercode" && \
    gradle-${GRADLE_VERSION}/bin/gradle -w fullBuild generateSwaggerDocumentation && \
    mv build/libs/clustercode.jar clustercode.jar && \
    mv build/swagger/* static/ && \
    echo "#### Installing node packages" && \
    npm install --quiet --no-optional && \
    echo "#### Bulding clustercode-admin" && \
    npm run build && \
    echo "#### Configuring" && \
    mv docker/nginx.conf /etc/nginx/nginx.conf && \
    mv docker/default . && \
    mv docker/docker-entrypoint.sh . && \
    mv docker/supervisord.conf . && \
    echo "#### Cleaning up" && \
    apk del nodejs-npm unzip && \
    rm -r ~/.npm && \
    rm -r node_modules && \
    rm -r package*.json && \
    rm -r .postcssrc.js && \
    rm -r .babelrc && \
    rm -r webpack* && \
    rm -r test && \
    rm -r static && \
    rm -r src && \
    rm -r *gradle* && \
    rm -r .gradle && \
    rm -r ~/.gradle && \
    rm -r build && \
    rm -r docker && \
    rm -r Dockerfile && \
    echo "#### Final file structure:" && \
    tree
