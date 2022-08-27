MAKEFLAGS += --warn-undefined-variables
SHELL := bash
.SHELLFLAGS := -eu -o pipefail -c
.DEFAULT_GOAL := help
.DELETE_ON_ERROR:
.SUFFIXES:

rbac_gen_src = ../../package/rbac/role.yaml
rbac_gen_tgt = templates/operator-clusterrole.yaml

ifeq ($(shell uname -s),Darwin)
	sed := gsed -i
else
	sed := sed -i
endif

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
prepare: $(rbac_gen_tgt) ## Helmify generated artifacts
