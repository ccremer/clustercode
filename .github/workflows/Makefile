.PHONY: test_codeclimate
codeclimate-test: integration-test ## Run tests for CodeClimate reporting
	@sed -i "s%github.com/ccremer/%%" cover.out

workflow_master_url := 	https://api.github.com/repos/$(DOCUMENTATION_REPOSITORY)/actions/workflows/build.yml/dispatches
workflow_tag_url := 	https://api.github.com/repos/$(DOCUMENTATION_REPOSITORY)/actions/workflows/tag.yml/dispatches

workflow_master_payload := '{"ref": "$(DOCUMENTATION_REF)"}'
workflow_tag_payload := '{"ref": "$(DOCUMENTATION_REF)", "inputs": {"new_tag": "$(DOCUMENTATION_TAG)"}}'

# Try removing -f flag from curl to debug responses
workflow_curl_args := curl -fS -XPOST -u "x-access-token:$(DOCUMENTATION_API_TOKEN)" \
		-H "Accept: application/vnd.github.v3+json" \
		-H "Content-Type: application/json"

workflow_master_cmd := $(workflow_curl_args) $(workflow_master_url) --data $(workflow_master_payload)
workflow_tag_cmd := $(workflow_curl_args) $(workflow_tag_url) --data $(workflow_tag_payload)

trigger_workflow_master: ## Trigger workflow for documentation builds from master branch
	@$(workflow_master_cmd)

trigger_workflow_tag: ## Trigger workflow for documentation builds for tags
	@$(workflow_tag_cmd)
