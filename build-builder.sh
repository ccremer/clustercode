#!/usr/bin/env bash

# original author  : paulfantom
# Cross-arch docker build helper script

REPOSITORY="${DOCKER_REPOSITORY}"
VERSION=""

docker run --rm --privileged multiarch/qemu-user-static:register --reset

docker build --tag "${REPOSITORY}:builder" --file builder.Dockerfile ./

# Check if we need to push images
if [ -z ${DOCKER_USERNAME+x} ]; then
    echo "No docker hub username  specified. Exiting without pushing images to registry"
    exit 0
fi
if [ -z ${DOCKER_PASSWORD+x} ]; then
    echo "No docker hub password specified. Exiting without pushing images to registry"
    exit 0
fi

# Login
echo "${DOCKER_PASSWORD}" | docker login -u "${DOCKER_USERNAME}" --password-stdin

# Push builder image
docker push "${REPOSITORY}:builder${VERSION}"