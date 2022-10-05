docs_output_dir := $(WORK_DIR)/docs

clean_targets += .docs-clean

.PHONY: docs-build
docs-build: export ANTORA_OUTPUT_DIR = $(docs_output_dir)
docs-build: docs/node_modules ## Build Antora documentation
	npm --prefix ./docs run build

.PHONY: docs-preview
docs-preview: export ANTORA_OUTPUT_DIR = $(docs_output_dir)
docs-preview: docs-build ## Preview Antora build in local web server and browser
	npm --prefix ./docs run preview

.PHONY: docs-publish
docs-publish: export ANTORA_OUTPUT_DIR = $(docs_output_dir)
docs-publish: docs-build | $(docs_output_dir) ## Publishes the documentation in gh-pages
	touch $(docs_output_dir)/.nojekyll
	wget -O $(docs_output_dir)/index.yaml https://raw.githubusercontent.com/$(PROJECT_OWNER)/$(PROJECT_NAME)/gh-pages/index.yaml
	npm --prefix ./docs run deploy

.PHONY: .docs-clean
.docs-clean: ## Clean documentation artifacts
	rm -rf $(ANTORA_OUTPUT_DIR) docs/node_modules

# Download node packages
docs/node_modules:
	npm --prefix ./docs install

$(docs_output_dir):
	mkdir -p $@
