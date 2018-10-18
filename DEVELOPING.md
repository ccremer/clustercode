# DEVELOPING

This file should cover the installation and guide on how to setup a
development environment.

## IDE

Though work has undergone to make this as generic as possible using
Gradle tasks, clustercode was built with JetBrain's IntellJ (Community).

## CI

Not a real CI/CD pipeline, but this project is configured and optimized
for Docker usage. As such, it is built and distributed on Docker Cloud.

The Docker build is always executing the unit tests. If you contribute
code, make sure the unit tests are all green when executing
`gradle test` before committing.

## Docker

On a Linux box with docker installed, run

    export DOCKER_REPOSITORY=braindoctor/clustercode; ./.travis/build.sh

which builds the builder image (a cache image that has all the Java
dependencies installed) and the multi-arch runtime images.

After you've built the builder image, you can just run

    docker build --build-arg ARCH="amd64-edge" --tag "${DOCKER_REPOSITORY}" .

to re-build the image locally for amd64.
