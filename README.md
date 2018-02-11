# clustercode

Automatically convert your movies and TV shows from one file format to another using ffmpeg in a cluster.

![clustercode_webadmin](https://user-images.githubusercontent.com/12159026/31952107-193afa02-b8e0-11e7-9f88-8d3d20e0d84c.png)

## Features

* Scans and encodes video files from a directory and encodes them using customizable profiles.
* Encoded files are stored in an output directory.
* Take advantage of having multiple computers: Each node encodes a video, enabling parallelization.
* Works as a single node too.
* No designated master. All nodes share the same state.
* Supports arbiter nodes for providing a quorum. Quorums are needed to prevent a split-brain. Useful if you
have a spare Raspberry Pi or NAS that is just poor at encoding.
* Several and different cleanup strategies.
* Supports Handbrake and ffmpeg
* Basic REST API

## Installation

* The recommended platform is Docker.
* Windows (download zip from releases tab).
* Build it using Gradle if you prefer it another way.

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
      - "8080:8080"
    volumes:
      - "/path/to/input:/input"
      - "/path/to/output:/output"
      - "/path/to/profiles:/profiles"
# If you need modifications to the xml files, persist them:
#      - "/path/to/config:/usr/src/clustercode/config"
    environment:
    # overwrite any settings from the default using env vars!
      - CC_CLUSTER_JGROUPS_TCP_INITIAL_HOSTS=your.other.docker.node[7600],another.one[7600]
      - CC_CLUSTER_JGROUPS_EXT_ADDR=192.168.1.100
```
The external IP address is needed so that other nodes will be available to
contact the local node. Use the physical address of the docker host.

### Docker Compose, Swarm mode

**This is untested**, as I don't have a Swarm. Just make sure to limit the CPU
resources somehow, so that other containers still work reliably. I'm assuming that
encoding is a low-priority service that takes forever anyway.
```
version: "3.2"
services:
  clustercode:
    image: braindoctor/clustercode:stable
    ports:
      - "7600:7600/tcp"
      - "7600:7600/udp"
    volumes:
      - "/path/to/input:/input"
      - "/path/to/output:/output"
      - "/path/to/profiles:/profiles"
# If you need modifications to the xml files, persist them:
#      - "/path/to/config:/usr/src/clustercode/config"
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

When you first start the container using docker compose, it will create a default configuration
file in `/usr/src/clustercode/config` (in the container). You can view the settings in the
`clustercode.properties` file and deviate from the default behaviour of the software. However, you should
modify the settings via Environment variables (same key/values syntax). Environment variables **always take precedence**
over the ones in `clustercode.properties`. If you made changes to the XML files, you need to mount a path from outside
in order to have them persistent.

## Project status

Sporadic Maintenance as of February 2018.

## Future Plans

- [x] Monitoring with a REST API.
- [x] [netdata](https://my-netdata.io/) plugin for progress monitoring.
- [x] Smooth-ier Windows deployment.
- [x] Web-Admin
- [ ] Remove the state-machine library, replace it with RxJava.
- [ ] Separate the docker image into base, backend and frontend images.
- [ ] More node control features in Web-Admin.

## Docker Tags

* experimental: latest automated build of the master branch
* latest: stable build of a tagged commit from a release
* tagged: tags following the 1.x.x pattern are specific releases

## SSL

The REST API and WebAdmin are easy to support with SSL/https. Just put a reverse proxy in front of clustercode
that handles https client connections and forwards the request via http to clustercode.
Check out https://github.com/jwilder/nginx-proxy for an excellent docker nginx proxy with SSL support.

The cluster communication is more difficult to set up with encryption. Even though the
traffic is binary and hard enough to intercept, it is not encrypted by default. You need
to change the JGroups configuration. Instructions can be found
[in the manual](http://jgroups.org/manual4/index.html#Security).

Generally this image and software is built with flexibility and simplicity in mind, not security.
Use it at your own risk.
