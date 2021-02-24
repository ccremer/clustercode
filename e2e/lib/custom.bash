#!/bin/bash

setup_file() {
    debug "-- $BATS_TEST_DESCRIPTION"
    debug "-- $(date)"
    debug ""
    debug ""
    export TEST_FILE_ID="$(basename ${BATS_TEST_FILENAME} .bats)"
    test_file=debug/$TEST_FILE_ID.yaml
    setup_file=debug/$TEST_FILE_ID-setup.yaml
    go run sigs.k8s.io/kustomize/kustomize/v3 build ${TEST_FILE_ID} -o ${test_file}
    go run sigs.k8s.io/kustomize/kustomize/v3 build setup -o ${setup_file}
    sed -i -e "s|\$E2E_IMAGE|${E2E_IMAGE}|" ${setup_file}
    sed -i -e "s/\TEST_FILE_ID/${TEST_FILE_ID}/" -e "s/\TEST_NAMESPACE/${DETIK_CLIENT_NAMESPACE}/" ${test_file} ${setup_file}
    run kubectl apply -f ${setup_file}
    debug "$output"

    try "at most 10 times every 2s to find 1 pod named '${TEST_FILE_ID}-operator' with '.spec.containers[*].image' being '${E2E_IMAGE}'"
    try "at most 10 times every 2s to find 1 pod named '${TEST_FILE_ID}-operator' with 'status' being 'running'"
}

teardown_file() {
  #run kubectl delete -f ${setup_file}
  debug "$output"
  cp -r /tmp/detik debug || true
}
