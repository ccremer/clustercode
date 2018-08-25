FROM openjdk:8-jdk

WORKDIR /usr/local/src/clustercode

COPY / .


RUN \
    ls -lah && \
    sh ./gradlew resolveDependencies
