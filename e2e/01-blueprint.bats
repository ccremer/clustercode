#!/usr/bin/env bats

load "lib/utils"
load "lib/detik"
load "lib/custom"

DETIK_CLIENT_NAME="kubectl"
DETIK_CLIENT_NAMESPACE="e2e-subject"
DEBUG_DETIK="true"

@test "Given Blueprint, When scheduling scan job, Then Job should succeed" {
    given_running_operator
    kubectl delete namespace "$DETIK_CLIENT_NAMESPACE" --ignore-not-found
    kubectl delete pv --all
	kubectl create namespace -o yaml --dry-run=client --save-config "$DETIK_CLIENT_NAMESPACE" | kubectl apply -f -

    apply definitions/blueprint
    try "at most 20 times every 5s to find 1 pod named 'test-blueprint-scan-job' with 'status' being 'Succeeded'"
}
