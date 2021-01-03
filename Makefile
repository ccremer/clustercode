# Set Shell to bash, otherwise some targets fail with dash/zsh etc.
SHELL := /bin/bash

include Makevariables.mk
include .github/workflows/Makefile
include docs/Makefile
include e2e/Makefile

build_cmd ?= CGO_ENABLED=0 go build -o $(BIN_FILENAME) main.go

# Get the currently used golang install path (in GOPATH/bin, unless GOBIN is set)
ifeq (,$(shell go env GOBIN))
GOBIN=$(shell go env GOPATH)/bin
else
GOBIN=$(shell go env GOBIN)
endif

all: build ## Invokes the build target

test: fmt vet ## Run tests
	go test ./... -coverprofile cover.out

$(TESTBIN_DIR):
	mkdir -p $(TESTBIN_DIR)

integration_test: export ENVTEST_K8S_VERSION = 1.19.2
integration_test: generate fmt vet $(TESTBIN_DIR) ## Run integration tests with envtest
	test -f ${ENVTEST_ASSETS_DIR}/setup-envtest.sh || curl -sSLo ${ENVTEST_ASSETS_DIR}/setup-envtest.sh https://raw.githubusercontent.com/kubernetes-sigs/controller-runtime/master/hack/setup-envtest.sh
	source ${ENVTEST_ASSETS_DIR}/setup-envtest.sh; fetch_envtest_tools $(ENVTEST_ASSETS_DIR); setup_envtest_env $(ENVTEST_ASSETS_DIR); go test -tags=integration -v ./... -coverprofile cover.out

build: generate fmt vet ## Build manager binary
	$(build_cmd)

run: export BACKUP_ENABLE_LEADER_ELECTION = $(ENABLE_LEADER_ELECTION)
run: fmt vet ## Run against the configured Kubernetes cluster in ~/.kube/config
	go run ./main.go

install: generate ## Install CRDs into a cluster
	$(KUSTOMIZE) build $(CRD_ROOT_DIR)/v1 | kubectl apply -f -

uninstall: generate ## Uninstall CRDs from a cluster
	$(KUSTOMIZE) build $(CRD_ROOT_DIR)/v1 | kubectl delete -f -

deploy: generate ## Deploy controller in the configured Kubernetes cluster in ~/.kube/config
	cd config/manager && $(KUSTOMIZE) edit set image controller=${IMG}
	$(KUSTOMIZE) build config/default | kubectl apply -f -

generate: ## Generate manifests e.g. CRD, RBAC etc.
	@CRD_ROOT_DIR="$(CRD_ROOT_DIR)" go generate -tags=generate generate.go
	@rm config/*.yaml

crd: generate ## Generate CRD to file
	$(KUSTOMIZE) build $(CRD_ROOT_DIR) > $(CRD_FILE)

fmt: ## Run go fmt against code
	go fmt ./...

vet: ## Run go vet against code
	go vet ./...

lint: fmt vet ## Invokes the fmt and vet targets
	@echo 'Check for uncommitted changes ...'
	git diff --exit-code

blank-media:
	@mkdir data/source data/intermediate data/target || true
	docker run --rm -it -u $(shell id -u) -v $(PWD)/data/source:/data $(FFMPEG_IMG) -y -hide_banner -t 30 -f lavfi -i color=c=black:s=320x240 -c:v libx264 -tune stillimage -pix_fmt yuv420p /data/blank_video.mp4
	@docker run --rm -it -v $(PWD)/data/source:/data --entrypoint /bin/sh $(FFMPEG_IMG) -c "chmod -R 664 /data/*"

clean-media:
	rm -r data/intermediate data/target || true

# Build the binary without running generators
.PHONY: $(BIN_FILENAME)
$(BIN_FILENAME):
	$(build_cmd)

docker-build: export GOOS = linux
docker-build: $(BIN_FILENAME) ## Build the docker image
	docker build . -t $(DOCKER_IMG) -t $(QUAY_IMG) -t $(E2E_IMG)

docker-push: ## Push the docker image
	docker push $(DOCKER_IMG)
	docker push $(QUAY_IMG)

clean: export KUBECONFIG = $(KIND_KUBECONFIG)
clean: clean-media clean-e2e ## Cleans up the generated resources
	rm -r testbin/ dist/ bin/ cover.out $(BIN_FILENAME) || true

.PHONY: help
help: ## Show this help
	@grep -E -h '\s##\s' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-20s\033[0m %s\n", $$1, $$2}'
