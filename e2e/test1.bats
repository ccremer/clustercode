#!/usr/bin/env bats

load "lib/utils"
load "lib/detik"
load "lib/custom"

DETIK_CLIENT_NAME="kubectl"
DETIK_CLIENT_NAMESPACE="clustercode-test1"
DEBUG_DETIK="true"

setup() {
    reset_debug
    run kubectl apply -f debug/${TEST_FILE_ID}.yaml
    debug "$output"

}

@test "Given ClustercodePlan, When scheduling scan job, then Job should succeed" {
    try "at most 20 times every 5s to find 1 pod named 'test-plan-scan-job' with 'status' being 'Succeeded'"
}

teardown() {
    run kubectl delete -f debug/${TEST_FILE_ID}.yaml
    debug "$output"
}
