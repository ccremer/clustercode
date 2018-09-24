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

    docker run --rm --privileged multiarch/qemu-user-static:register --reset

to enable multiarch builds.