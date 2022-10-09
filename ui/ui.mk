# These make target are generally only needed by CI/CD
# Use npm directly for local development

clean_targets += clean-ui

npm = npm --prefix ./ui
npm_run = $(npm) run

ui/node_modules:
	$(npm) install

ui/dist:
	mkdir -p $@/assets

.PHONY: lint-ui
lint-ui: ui/node_modules ## Runs linters for the UI code
	$(npm_run) lint
	$(npm_run) check

.PHONY: test-ui
test-ui: ui/node_modules ## Runs tests for the UI code
	$(npm_run) cy:run

.PHONY: build-ui
build-ui: ui/node_modules ## Builds the UI for packaging
	$(npm_run) build

.PHONY: clean-ui
clean-ui: ## Removes all UI-related artifacts (node_modules, dist)
	rm -rf ui/node_modules ui/dist ui/.env

.PHONY: run-ui
run-ui: ui/.env ## Prepares the local environment to run the UI in development mode including kind cluster
	$(npm_run) dev

ui/.env: $(KIND_KUBECONFIG)
	@echo "VITE_KUBERNETES_API_URL=$$(yq e '.clusters[0].cluster.server' $(KIND_KUBECONFIG))" > $@
