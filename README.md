# clustercode

Automatically convert your movies and TV shows from one file format to another using ffmpeg in a cluster.

## Features

* Scans and encodes video files from a directory and encodes them using customizable profiles.
* Encoded files are stored in an output directory.
* Major video formats supported: x264, x265 (HEVC), ...
* Take advantage of having multiple computers: Each node encodes a video, enabling parallelization.
* Works as a single node too
* No designated master. Whoever is "first" in the network, becomes master
* Supports arbiter nodes for providing a quorum. Quorums are needed to prevent a split-brain. Useful if you have a spare Raspberry Pi that is just poor at encoding.
* Several and different cleanup strategies.
* Supports Handbrake and ffmpeg

## Installation

Currently, only on Docker. You have to build it using (Maven) if you prefer it another way. Works with Windows too (its Java), but you need to figure out who to deploy it reasonably. Better support is planned, but Docker takes priority.

I hate long `docker run` commands with tons of arguments, so here is a docker-compose template:

### Docker Compose, non-swarm mode

```yaml
version: "2.2"
services:
  clustercode:
    restart: always
    image: braindoctor/clustercode:latest
    container_name: clustercode
    cpu_shares: 512
    ports:
      - "7600:7600/tcp"
    volumes:
      - "/path/to/input:/input"
      - "/path/to/output:/output"
      - "/path/to/config:/usr/src/clustercode/config"
    environment:
      - CC_CLUSTER_JGROUPS_TCP_INITAL_HOSTS=your.other.docker.node[7600],another.one[7600]
      - CC_CLUSTER_JGROUPS_EXT_ADDR=192.168.1.100
```

### Docker Compose, Swarm mode

This is untested, as I don't have a Swarm. Just make sure that limit the CPU resources somehow, so that other containers still work reliably. I figure that encoding is a low-priority service that takes forever anyway.
```
version: "3.2"
services:
  clustercode:
    image: braindoctor/clustercode:latest
    ports:
      - "7600:7600/tcp"
      - "7600:7600/udp"
    volumes:
      - "/path/to/input:/input"
      - "/path/to/output:/output"
      - "/path/to/config:/usr/src/clustercode/config"
    deploy:
      restart_policy:
        condition: any
        max_attempts: 3
        window: 30s
        delay: 3s
      resources:
        limits:
          cpus: "3"
```

## Configuration

When you first start the container using docker compose, it will create a default configuration file in `/usr/src/clustercode/config` (in the container). Be sure that you have mounted this dir from outside for persistence and it's empty. You can change the settings in the `clustercode.properties` file to modify the behaviour of the software.

Alternatively you can configure all settings via Environment variables (same key/values syntax). Environment variables **always take precendence** over the ones in `clustercode.properties`.

## Project status

Active Development as of August/September 2017.

## Future Plans

* Monitoring with a REST API.
* [netdata](https://my-netdata.io/) plugin for monitoring.
* Smooth-ier Windows deployment.
