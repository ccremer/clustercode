# These make target are generally only needed by CI/CD
# Use npm directly for local development

npm = npm --prefix ./ui
npm_run = $(npm) run

ui/node_modules:
	$(npm) install

.PHONY: lint-ui
lint-ui: ui/node_modules ## Runs linters for the UI code
	$(npm_run) lint
	$(npm_run) check

.PHONY: test-ui
test-ui: ui/node_modules ## Runs tests for the UI code
	$(npm_run) cy:run
