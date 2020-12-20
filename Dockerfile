FROM docker.io/library/alpine:3.12 as runtime

ENTRYPOINT ["clustercode"]

RUN \
    apk add --no-cache curl bash tzdata

COPY clustercode /usr/bin/
USER 1001:0
