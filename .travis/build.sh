#!/usr/bin/env bash

repo="${DOCKER_REPOSITORY}"

# Install cross-build libraries
docker run --rm --privileged multiarch/qemu-user-static:register --reset

# Build builder image
docker build --tag "${repo}:builder" --file ./builder.Dockerfile ./

# Build runtime images
for ARCH in armhf amd64 i386 aarch64; do
    tag="${ARCH}"
    docker build --build-arg ARCH="${ARCH}-edge" --tag "${repo}:${tag}" --file ./Dockerfile ./
done
