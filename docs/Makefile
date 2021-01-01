# Set Shell to bash, otherwise some targets fail with dash/zsh etc.
SHELL := /bin/bash

DOCS_DIR ?= .

NEW_TAG ?= ""

ANTORA_PLAYBOOK_PATH ?= antora-playbook.yml
ANTORA_OUTPUT_DIR ?= $(DOCS_DIR)/public

docs-build: $(DOCS_DIR)/node_modules ## Install dependencies to create docs
	cd $(DOCS_DIR) && node_modules/@antora/cli/bin/antora $(ANTORA_PLAYBOOK_PATH)
	@touch $(ANTORA_OUTPUT_DIR)/.nojekyll

$(DOCS_DIR)/node_modules:
	cd $(DOCS_DIR) && npm install

docs-add-tag: ## Adds a new version tag to the first Antora component (set NEW_TAG=<your-tag>)
	cd $(DOCS_DIR) && yq eval -i '.content.sources.[0].branches += "$(NEW_TAG)"' $(ANTORA_PLAYBOOK_PATH)

docs-clean:
	rm -r $(ANTORA_OUTPUT_DIR) || true
