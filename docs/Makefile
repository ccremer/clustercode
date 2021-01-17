# Set Shell to bash, otherwise some targets fail with dash/zsh etc.
SHELL := /bin/bash

# Disable built-in rules
MAKEFLAGS += --no-builtin-rules
MAKEFLAGS += --no-builtin-variables
.SUFFIXES:
.SECONDARY:

ANTORA_PLAYBOOK_PATH ?= local-antora-playbook.yml
ANTORA_OUTPUT_DIR ?= $(shell grep "dir" $(ANTORA_PLAYBOOK_PATH) | cut -d " " -f 4)

.PHONY: build
build: node_modules ## Build Antora documentation
	npm run build

node_modules:
	npm install

.PHONY: clean
clean: ## Clean documentation artifacts
	rm -r $(ANTORA_OUTPUT_DIR) node_modules || true

.PHONY: preview
preview: build ## Preview Antora build in local web server and browser
	npm run preview

.PHONY: help
help: ## Show this help
	@grep -E -h '\s##\s' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-20s\033[0m %s\n", $$1, $$2}'
