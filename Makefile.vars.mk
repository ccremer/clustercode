## These are some common variables for Make

PROJECT_ROOT_DIR = .
PROJECT_NAME ?= clustercode
PROJECT_OWNER ?= ccremer

WORK_DIR = $(PWD)/.work

## BUILD:go
BIN_FILENAME ?= $(PROJECT_NAME)
go_bin ?= $(WORK_DIR)/bin
$(go_bin):
	@mkdir -p $@

## BUILD:docker
DOCKER_CMD ?= docker

IMG_TAG ?= latest
CONTAINER_REGISTRY ?= ghcr.io
# Image URL to use all building/pushing image targets
CONTAINER_IMG ?= $(CONTAINER_REGISTRY)/$(PROJECT_OWNER)/$(PROJECT_NAME):$(IMG_TAG)

## KIND:setup

# https://hub.docker.com/r/kindest/node/tags
KIND_NODE_VERSION ?= v1.24.0
KIND_IMAGE ?= docker.io/kindest/node:$(KIND_NODE_VERSION)
KIND_KUBECONFIG ?= $(kind_dir)/kind-kubeconfig
KIND_CLUSTER ?= $(PROJECT_NAME)

# TEST:integration
ENVTEST_ADDITIONAL_FLAGS ?= --bin-dir "$(go_bin)"
# See https://storage.googleapis.com/kubebuilder-tools/ for list of supported K8s versions
ENVTEST_K8S_VERSION = 1.24.x
INTEGRATION_TEST_DEBUG_OUTPUT ?= false

## MEDIA:
FFMPEG_IMG ?= ghcr.io/jrottenberg/ffmpeg:5.0-alpine
