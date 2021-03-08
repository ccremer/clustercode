#!/usr/bin/env bats

load "lib/utils"
load "lib/detik"
load "lib/custom"

DETIK_CLIENT_NAME="kubectl"
DETIK_CLIENT_NAMESPACE="clustercode-system"
DEBUG_DETIK="true"

@test "Given Operator deployment, When running Pod, then expect Pod to become ready" {
	# Remove traces of operator from other tests
	kubectl delete namespace "$DETIK_CLIENT_NAMESPACE" --ignore-not-found
	kubectl create namespace -o yaml --dry-run=client --save-config "$DETIK_CLIENT_NAMESPACE" | kubectl apply -f -

	apply definitions/operator

	try "at most 10 times every 2s to find 1 pod named 'operator' with '.spec.containers[*].image' being '${E2E_IMAGE}'"
	try "at most 20 times every 2s to find 1 pod named 'operator' with 'status' being 'running'"
}
