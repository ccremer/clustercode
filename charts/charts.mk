helm_docs_bin := $(go_bin)/helm-docs

clean_targets += chart-clean

# Prepare binary
$(helm_docs_bin): export GOBIN = $(go_bin)
$(helm_docs_bin): | $(go_bin)
	go install github.com/norwoodj/helm-docs/cmd/helm-docs@latest

.PHONY: chart-generate
chart-generate: .github/crds.yaml ## Prepare the Helm charts
	@find charts -type f -name Makefile | sed 's|/[^/]*$$||' | xargs -I '%' $(MAKE) -C '%' prepare

.PHONY: chart-docs
chart-docs: $(helm_docs_bin) ## Creates the Chart READMEs from template and values.yaml files
	@$(helm_docs_bin) \
		--template-files ./.github/helm-docs-header.gotmpl.md \
		--template-files README.gotmpl.md \
		--template-files ./.github/helm-docs-footer.gotmpl.md

.PHONY: chart-lint
chart-lint: chart-generate chart-docs ## Lint charts
	@echo 'Check for uncommitted changes ...'
	git diff --exit-code

.PHONY: chart-clean
chart-clean: ## Clean the Helm chart artifacts
	rm -rf $(helm_docs_bin) .cr-index .cr-release-packages charts/*/CHANGELOG.md

.PHONY: chart-release
chart-release: | .cr-index ## Release the Helm chart to GitHub
# CHART_NAME is given by GH action
# Download 'cr' manually from https://github.com/helm/chart-releaser/releases, 'go install' doesn't work...
	cr package charts/$(CHART_NAME)
	cr upload "--release-name-template=chart/{{ .Name }}-{{ .Version }}" --release-notes-file=CHANGELOG.md
	cr index  "--release-name-template=chart/{{ .Name }}-{{ .Version }}" --push

.cr-index:
	mkdir -p $@
