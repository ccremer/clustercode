chart_deploy_args =

.PHONY: chart-deploy
chart-deploy: export KUBECONFIG = $(KIND_KUBECONFIG)
chart-deploy: go_build_args = -tags=ui
chart-deploy: blank-media build-ui kind-load-image kind-setup-ingress install-crd webhook-cert ## Install Operator in local cluster
	helm upgrade --install clustercode ./charts/clustercode \
		--create-namespace --namespace clustercode-system \
		--set podAnnotations.sha="$(shell docker inspect $(CONTAINER_IMG) | jq -r '.[].Id')" \
		--values test/values.yaml \
		--values $(webhook_values) \
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
# kuttl leaves kubeconfig garbage: https://github.com/kudobuilder/kuttl/issues/297
	@rm -f kubeconfig

clean_targets += .e2e-test-clean
.PHONY: .e2e-test-clean
.e2e-test-clean:
	rm -f $(kuttl_bin)

###
### Generate webhook certificates
###

tls_dir = $(WORK_DIR)/tls
webhook_key = $(tls_dir)/tls.key
webhook_cert = $(tls_dir)/tls.crt
webhook_service_name = clustercode-webhook.clustercode-system.svc
webhook_values = $(tls_dir)/webhook-values.yaml

$(tls_dir):
	mkdir -p $@

.PHONY: webhook-cert
webhook-cert: $(webhook_values)

$(webhook_key): | $(tls_dir)
	openssl req -x509 -newkey rsa:4096 -nodes -keyout $@ --noout -days 3650 -subj "/CN=$(webhook_service_name)" -addext "subjectAltName = DNS:$(webhook_service_name)"

$(webhook_cert): $(webhook_key)
	openssl req -x509 -key $(webhook_key) -nodes -out $@ -days 3650 -subj "/CN=$(webhook_service_name)" -addext "subjectAltName = DNS:$(webhook_service_name)"

$(webhook_values): $(webhook_cert)
	@yq -n '.webhook.caBundle="$(shell base64 -w0 $(webhook_cert))" | .webhook.certificate="$(shell base64 -w0 $(webhook_cert))" | .webhook.privateKey="$(shell base64 -w0 $(webhook_key))"' > $@

clean_targets += .webhook-clean
.PHONY: .webhook-clean
.webhook-clean:
	rm -rf $(tls_dir)
