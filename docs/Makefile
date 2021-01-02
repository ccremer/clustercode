# Set Shell to bash, otherwise some targets fail with dash/zsh etc.
SHELL := /bin/bash

DOCS_DIR ?= .

ANTORA_PLAYBOOK_PATH ?= local-antora-playbook.yml
ANTORA_OUTPUT_DIR ?= $(shell cd $(DOCS_DIR) && grep dir $(ANTORA_PLAYBOOK_PATH) | cut -d " " -f 4)

docs-build: $(DOCS_DIR)/node_modules
	cd $(DOCS_DIR) && npm run build

$(DOCS_DIR)/node_modules:
	cd $(DOCS_DIR) && npm install

docs-clean:
	cd $(DOCS_DIR) && rm -r $(ANTORA_OUTPUT_DIR) node_modules || true

docs-preview: docs-build ## Preview Antora build in local web server
	cd $(DOCS_DIR) && npm run serve
