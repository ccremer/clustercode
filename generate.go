// +build generate

package main

//go:generate go run sigs.k8s.io/controller-tools/cmd/controller-gen object:headerFile="kustomize/boilerplate.go.txt" paths="./..."
//go:generate go run sigs.k8s.io/controller-tools/cmd/controller-gen crd:trivialVersions=true rbac:roleName=manager-role webhook paths="./..." output:crd:artifacts:config=${CRD_ROOT_DIR}/v1alpha1 crd:crdVersions=v1 output:dir=kustomize

import (
	_ "sigs.k8s.io/controller-tools/cmd/controller-gen"
	_ "sigs.k8s.io/kustomize/kustomize/v3"
)
