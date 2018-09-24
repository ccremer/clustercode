#!/usr/bin/env bash

# original author  : paulfantom
# Cross-arch docker build helper script

REPOSITORY="${DOCKER_REPOSITORY}"
VERSION=""

docker run --rm --privileged multiarch/qemu-user-static:register --reset

# Build runtime images
for ARCH in armhf amd64 i386 aarch64; do
     docker build --build-arg ARCH="${ARCH}-edge" --tag "${REPOSITORY}:${ARCH}${VERSION}" --file Dockerfile ./
done
docker tag "${REPOSITORY}:amd64" "${REPOSITORY}:latest"

# Check if we need to push images
if [ -z ${DOCKER_USERNAME+x} ]; then
    echo "No docker hub username  specified. Exiting without pushing images to registry"
    exit 0
fi
if [ -z ${DOCKER_PASSWORD+x} ]; then
    echo "No docker hub password specified. Exiting without pushing images to registry"
    exit 0
fi

# Push runtime images
echo "${DOCKER_PASSWORD}" | docker login -u "${DOCKER_USERNAME}" --password-stdin
for ARCH in amd64 armhf i386 aarch64; do
    docker push "${REPOSITORY}:${ARCH}${VERSION}"
done
docker push "${REPOSITORY}:latest"
