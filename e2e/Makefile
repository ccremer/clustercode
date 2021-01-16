
# Set Shell to bash, otherwise some targets fail with dash/zsh etc.
SHELL := /bin/bash

E2E_DIR ?= .

#### Usable for the developer
setup_e2e_test: $(setup_e2e_test) ## Run the e2e setup

e2e_test: export E2E_IMAGE = $(E2E_IMG)
e2e_test: install_bats $(setup_e2e_test) $(KIND_KUBECONFIG)  ## Runs the e2e test suite (this is the one you want)
e2e_test: docker-build kind_load_image run_bats

clean_e2e_setup: export KUBECONFIG = $(KIND_KUBECONFIG)
clean_e2e_setup: ## Clean the e2e setup (e.g. to rerun the setup_e2e_test)
	kubectl delete ns $(OPERATOR_NAMESPACE) --ignore-not-found --force --grace-period=0 || true
	@$(KUSTOMIZE_BUILD_CRD) | kubectl delete -f - || true
	@rm $(setup_e2e_test) $(E2E_DIR)/debug $(E2E_DIR)/node_modules || true

run_kind: export KUBECONFIG = $(KIND_KUBECONFIG)
run_kind: $(setup_e2e_test) ## Runs the operator in kind
	go run ./main.go -v operate --operator.clustercode-image=$(E2E_IMG)
####



#### Everything BATS related
.PHONY: run_bats install_bats

bats_args ?=

run_bats: export KUBECONFIG = ../$(KIND_KUBECONFIG)
run_bats: install_bats ## Actually runs the bats test
	@mkdir -p $(E2E_DIR)/debug || true
	@cd $(E2E_DIR) && node_modules/.bin/bats . $(bats_args)

install_bats: $(E2E_DIR)/node_modules/.bin/bats ## Installs the bats util via NPM

$(E2E_DIR)/node_modules/.bin/bats: $(E2E_DIR)/node_modules

$(E2E_DIR)/node_modules:
	@cd $(E2E_DIR) && npm install
####



#### Everything KIND related
kind_bin ?= $(TESTBIN_DIR)/kind
setup_e2e_test := testbin/.setup_e2e_test

kind_load_image:
	@$(kind_bin) load docker-image --name $(KIND_CLUSTER) $(E2E_IMG)
	@docker rmi $(E2E_IMG)

$(kind_bin): export KUBECONFIG = $(KIND_KUBECONFIG)
$(kind_bin): $(TESTBIN_DIR)
	curl -fLo $(kind_bin) "https://kind.sigs.k8s.io/dl/v$(KIND_VERSION)/kind-$$(uname)-amd64"
	@chmod +x $(kind_bin)
	$(kind_bin) create cluster --name $(KIND_CLUSTER) --image kindest/node:$(KIND_NODE_VERSION) --config=e2e/kind-config.yaml
	@kubectl version
	@kubectl cluster-info

$(KIND_KUBECONFIG): $(kind_bin)

$(setup_e2e_test): export KUBECONFIG = $(KIND_KUBECONFIG)
$(setup_e2e_test): $(kind_bin)
	@kubectl config use-context kind-$(KIND_CLUSTER)
	@$(KUSTOMIZE_BUILD_CRD) | kubectl apply $(KIND_KUBECTL_ARGS) -f -
	@touch $(setup_e2e_test)

clean-e2e:
	$(kind_bin) delete cluster --name $(KIND_CLUSTER) || true
	docker images --filter "reference=$(E2E_REPO)" --format "{{.Repository }}:{{ .Tag }}" | xargs --no-run-if-empty docker rmi || true
####
