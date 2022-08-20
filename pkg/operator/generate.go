//go:build generate

// Generate manifests
//go:generate go run sigs.k8s.io/controller-tools/cmd/controller-gen rbac:roleName=manager-role paths="./..." output:crd:artifacts:config=${CRD_ROOT_DIR}/v1alpha1 crd:crdVersions=v1 output:dir=../../package/rbac

package operator
