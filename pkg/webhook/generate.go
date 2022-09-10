//go:build generate

// Remove existing manifests
//go:generate rm -rf ../../package/webhook

// Generate webhook manifests
//go:generate go run -tags generate sigs.k8s.io/controller-tools/cmd/controller-gen webhook paths=./... output:artifacts:config=../../package/webhook

package webhook
