//go:build generate

// Remove existing manifests
//go:generate rm -rf ../../package/crds ../../package/rbac

// Generate deepcopy methodsets and CRD manifests
//go:generate go run -tags generate sigs.k8s.io/controller-tools/cmd/controller-gen object:headerFile=../../.github/boilerplate.go.txt paths=./... crd:crdVersions=v1 output:artifacts:config=../../package/crds

package api
