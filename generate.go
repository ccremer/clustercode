// +build generate

package main

//go:generate go run sigs.k8s.io/controller-tools/cmd/controller-gen object:headerFile="kustomize/boilerplate.go.txt" paths="./..."
//go:generate go run sigs.k8s.io/controller-tools/cmd/controller-gen crd:trivialVersions=true rbac:roleName=manager-role webhook paths="./..." output:crd:artifacts:config=${CRD_ROOT_DIR}/v1alpha1 crd:crdVersions=v1 output:dir=kustomize

// Generate API reference documentation
//go:generate go run github.com/elastic/crd-ref-docs --source-path=api/v1alpha1 --config=docs/api-gen-config.yaml --renderer=asciidoctor --templates-dir=docs/api-templates --output-path=${CRD_DOCS_REF_PATH}

import (
	_ "github.com/elastic/crd-ref-docs"
	_ "sigs.k8s.io/controller-tools/cmd/controller-gen"
	_ "sigs.k8s.io/kustomize/kustomize/v3"
)
