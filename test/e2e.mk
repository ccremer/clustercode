chart_deploy_args =

.PHONY: chart-deploy
chart-deploy: export KUBECONFIG = $(KIND_KUBECONFIG)
chart-deploy: blank-media kind-load-image install-crd ## Install Operator in local cluster
	helm upgrade --install clustercode ./charts/clustercode \
		--create-namespace --namespace clustercode-system \
		--set podAnnotations.sha="$(shell docker inspect $(CONTAINER_IMG) | jq -r '.[].Id')" \
		--values test/values.yaml \
		--wait $(chart_deploy_args)

###
### E2E Tests
### with KUTTL (https://kuttl.dev)
###

kuttl_bin = $(go_bin)/kubectl-kuttl
$(kuttl_bin): export GOBIN = $(go_bin)
$(kuttl_bin): | $(go_bin)
	go install github.com/kudobuilder/kuttl/cmd/kubectl-kuttl@latest

test-e2e: export KUBECONFIG = $(KIND_KUBECONFIG)
test-e2e: $(kuttl_bin) chart-deploy ## Run E2E tests in local cluster
	@cp ./test/e2e/operator/03-install.yaml.template ./test/e2e/operator/03-install.yaml
	@yq -i e '.spec.encode.podTemplate.containers[0].securityContext.runAsUser=$(shell id -u)' ./test/e2e/operator/03-install.yaml
	@yq -i e '.spec.cleanup.podTemplate.containers[0].securityContext.runAsUser=$(shell id -u)' ./test/e2e/operator/03-install.yaml
	$(kuttl_bin) test ./test/e2e --config ./test/e2e/kuttl-test.yaml
	@rm -f kubeconfig
# kuttl leaves kubeconfig garbage: https://github.com/kudobuilder/kuttl/issues/297

.PHONY: .e2e-test-clean
.e2e-test-clean:
	rm -f $(kuttl_bin)
