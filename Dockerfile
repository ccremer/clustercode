FROM docker.io/library/alpine:3.17 as runtime

ENTRYPOINT ["clustercode"]

RUN \
    apk add --update --no-cache \
      bash \
      curl \
      ca-certificates \
      tzdata

COPY clustercode /usr/bin/
USER 65536:0
