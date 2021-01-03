VERSION ?= $(shell date +%Y-%m-%d_%H-%M-%S)
BIN_FILENAME ?= clustercode

DOCS_DIR := docs
E2E_DIR := e2e

IMG_TAG ?= latest
E2E_TAG ?= e2e_$(VERSION)
E2E_REPO ?= local.dev/clustercode/e2e

E2E_IMG := $(E2E_REPO):$(E2E_TAG)

CRD_SPEC_VERSION ?= v1
CRD_ROOT_DIR ?= config/crd/v1alpha1
CRD_FILE ?= clustercode-crd.yaml

OPERATOR_NAMESPACE ?= clustercode-system

TESTBIN_DIR ?= ./testbin/bin
KIND_VERSION ?= 0.9.0
KIND_KUBECONFIG ?= testbin/kind-kubeconfig
KIND_NODE_VERSION ?= v1.19.4
KIND_CLUSTER ?= clustercode-$(KIND_NODE_VERSION)
KIND_KUBECTL_ARGS ?= --validate=true

KUSTOMIZE ?= go run sigs.k8s.io/kustomize/kustomize/v3
KUSTOMIZE_BUILD_CRD ?= $(KUSTOMIZE) build $(CRD_ROOT_DIR)


# Image URL to use all building/pushing image targets
DOCKER_IMG ?= docker.io/ccremer/clustercode:$(IMG_TAG)
QUAY_IMG ?= quay.io/ccremer/clustercode:$(IMG_TAG)
E2E_IMG ?= localhost:$(KIND_REGISTRY_PORT)/clustercode/operator:e2e
FFMPEG_IMG ?= docker.io/jrottenberg/ffmpeg:4.1-alpine

# Run tests (see https://sdk.operatorframework.io/docs/building-operators/golang/references/envtest-setup)
ENVTEST_ASSETS_DIR=$(shell pwd)/testbin

# Trigger Documentation workflow in another repository
#
DOCUMENTATION_REPOSITORY ?= ccremer/clustercode-docs
# The git ref to run the workflow in
DOCUMENTATION_REF ?= master
# The new git tag to add
DOCUMENTATION_TAG ?=
# Set this in GH Action
DOCUMENTATION_API_TOKEN ?=
