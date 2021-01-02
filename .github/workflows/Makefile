.PHONY: test_codeclimate
test_codeclimate: test ## Run tests for CodeClimate
	@sed -i "s%github.com/ccremer/%%" cover.out

trigger_workflow: ## Trigger workflow for documentation builds
	@curl -XPOST -u "x-access-token:$(DOCUMENTATION_API_TOKEN)" \
		-H "Accept: application/vnd.github.v3+json" \
		-H "Content-Type: application/json" \
		https://api.github.com/repos/$(DOCUMENTATION_REPOSITORY)/actions/workflows/$(DOCUMENTATION_WORKFLOW)/dispatches \
		--data '{"ref": "$(DOCUMENTATION_REF)", "inputs": {"new_tag": "$(DOCUMENTATION_TAG)"}}'
