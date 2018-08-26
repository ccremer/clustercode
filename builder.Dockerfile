FROM openjdk:8-jdk

WORKDIR /usr/local/src/clustercode

RUN \
    apt-get update && \
    apt-get install tree

COPY / .

RUN \
    sh ./gradlew resolveDependencies && \
    # Don't need sources, it will get added anyway
    rm -r *