MAKEFLAGS += --warn-undefined-variables
SHELL := bash
.SHELLFLAGS := -eu -o pipefail -c
.DEFAULT_GOAL := help
.DELETE_ON_ERROR:
.SUFFIXES:

webhook_gen_src = ../../package/webhook/manifests.yaml
webhook_gen_tgt = templates/webhook/webhook-config.yaml

rbac_gen_src = ../../package/rbac/role.yaml
rbac_gen_tgt = templates/operator/clusterrole.yaml

ifeq ($(shell uname -s),Darwin)
	sed := gsed -i
else
	sed := sed -i
endif

.PHONY: $(webhook_gen_tgt)
$(webhook_gen_tgt):
	@cp $(webhook_gen_src) $@
	@yq -i e '.metadata.name="{{ include \"clustercode.fullname\" . }}", del(.metadata.creationTimestamp)' $@
	@yq -i e '.metadata.labels.replace="LABELS"' $@
	@yq -i e '.metadata.annotations.replace="ANNOTATIONS"' $@
	@yq -i e '.webhooks[0].clientConfig.caBundle="{{ . }}"' $@
	@yq -i e '.webhooks[0].clientConfig.service.name="{{ include \"clustercode.fullname\" . }}-webhook"' $@
	@yq -i e '.webhooks[0].clientConfig.service.namespace="{{ .Release.Namespace }}"' $@
	@$(sed) -e '1s/^/{{- if .Values.webhook.enabled -}}\n/' $@
	@$(sed) -e '/^      caBundle:.*/i {{- with .Values.webhook.caBundle }}' $@
	@$(sed) -e '/^      caBundle:.*/a {{- end }}' $@
	@$(sed) -e 's/replace: LABELS/{{- include "clustercode.labels" . | nindent 4 }}/g' $@
	@$(sed) -e 's/replace: ANNOTATIONS/{{- toYaml .Values.webhook.annotations | nindent 4 }}/g' $@
	@echo "{{- end -}}" >> $@

.PHONY: $(rbac_gen_tgt)
$(rbac_gen_tgt):
	@cp $(rbac_gen_src) $@
	@yq -i e '.metadata.name="{{ include \"clustercode.fullname\" . }}-manager", del(.metadata.creationTimestamp)' $@
	@yq -i e '.metadata.labels.replace="LABELS"' $@
	@$(sed) -e 's/replace: LABELS/{{- include "clustercode.labels" . | nindent 4 }}/g' $@

#
# "Interface" for parent Makefile
#
.PHONY: prepare
prepare: $(rbac_gen_tgt) $(webhook_gen_tgt) ## Helmify generated artifacts
