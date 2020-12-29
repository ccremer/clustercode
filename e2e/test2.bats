#!/usr/bin/env bats

load "lib/utils"
load "lib/detik"
load "lib/custom"

DETIK_CLIENT_NAME="kubectl"
DETIK_CLIENT_NAMESPACE="default"
DEBUG_DETIK="true"

@test "reset the debug file" {
	reset_debug
}

@test "verify the clustercode plan cronjob" {
  go run sigs.k8s.io/kustomize/kustomize/v3 build test2 > debug/test2.yaml
  sed -i "s/\$RANDOM/'$RANDOM'/" debug/test2.yaml
  run kubectl apply -f debug/test2.yaml
  debug "$output"

  try "at most 20 times every 5s to find 1 pod named 'test-plan-scan-job' with 'status' being 'Succeeded'"

}
