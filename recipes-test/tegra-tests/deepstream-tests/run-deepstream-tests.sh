#!/bin/sh
# SPDX-License-Identifier: MIT
# Copyright (c) 2022-2024, OpenEmbedded for Tegra Project
#
# WARNING: make sure you have at least a couple of GiB of free
# space availabe, as some of these samples need to download models/data.
#
# Some of these tests work better if you use jetson_clocks to
# speed things up.

DEEPSTREAM_PATH="/opt/nvidia/deepstream/deepstream-7.1"
SAMPLEROOT="${DEEPSTREAM_PATH}/sources/apps/sample_apps"
PYSAMPLEROOT="${DEEPSTREAM_PATH}/sources/deepstream_python_apps/apps"
STREAMS="${DEEPSTREAM_PATH}/samples/streams"
SKIPCODE=97

run_deepstream_test1() {
    echo "Running deepstream test1"
    if [ ! -d "${SAMPLEROOT}/deepstream-test1" ]; then
        return $SKIPCODE
    fi
    cd "${SAMPLEROOT}/deepstream-test1"
    deepstream-test1-app "${STREAMS}/sample_720p.h264"
}

run_deepstream_test2() {
    echo "Running deepstream test2"
    if [ ! -d "${SAMPLEROOT}/deepstream-test2" ]; then
        return $SKIPCODE
    fi
    cd "${SAMPLEROOT}/deepstream-test2"
    deepstream-test2-app "${STREAMS}/sample_720p.h264"
}

run_deepstream_test3() {
    echo "Running deepstream test3"
    if [ ! -d "${SAMPLEROOT}/deepstream-test3" ]; then
        return $SKIPCODE
    fi
    cd "${SAMPLEROOT}/deepstream-test3"
    deepstream-test3-app file://${STREAMS}/sample_1080p_h264.mp4 file://${STREAMS}/sample_720p.mp4
}

run_py_deepstream_test1() {
    echo "Running Python deepstream test1"
    if [ ! -d "${PYSAMPLEROOT}/deepstream-test1" ]; then
        return $SKIPCODE
    fi
    cd "${PYSAMPLEROOT}/deepstream-test1"
    python3 deepstream_test_1.py "${STREAMS}/sample_720p.h264"
}

run_py_deepstream_test2() {
    echo "Running Python deepstream test2"
    if [ ! -d "${PYSAMPLEROOT}/deepstream-test2" ]; then
        return $SKIPCODE
    fi
    cd "${PYSAMPLEROOT}/deepstream-test2"
    python3 deepstream_test_2.py "${STREAMS}/sample_720p.h264"
}

run_py_deepstream_test3() {
    echo "Running Python deepstream test3"
    if [ ! -d "${PYSAMPLEROOT}/deepstream-test3" ]; then
        return $SKIPCODE
    fi
    cd "${PYSAMPLEROOT}/deepstream-test3"
    python3 deepstream_test_3.py -i file://${STREAMS}/sample_1080p_h264.mp4 file://${STREAMS}/sample_720p.mp4
}

TESTS="deepstream_test1 deepstream_test2 deepstream_test3"
TESTS="$TESTS py_deepstream_test1 py_deepstream_test2 py_deepstream_test3"

find_test() {
    for t in $TESTS; do
    if [ "$t" = "$1" ]; then
        echo "$t"
        return
    fi
    done
}

jetson_clocks

testcount=0
testpass=0
testfail=0
testskip=0
if [ $# -eq 0 ]; then
    tests_to_run="$TESTS"
else
    tests_to_run="$@"
fi

for cand in $tests_to_run; do
    t=$(find_test "$cand")
    if [ -z "$t" ]; then
        echo "ERR: unknown test: $cand" >&2
    else
        testcount=$((testcount+1))
        echo "=== BEGIN: $t ==="
    if run_$t; then
        echo "=== PASS:  $t ==="
        testpass=$((testpass+1))
    elif [ $? -eq $SKIPCODE ]; then
        echo "=== SKIP:  $t ==="
        testskip=$((testskip+1))
    else
        echo "=== FAIL:  $t ==="
        testfail=$((testfail+1))
    fi
    fi
done

echo "Tests run:     $testcount"
echo "Tests passed:  $testpass"
echo "Tests skipped: $testskip"
echo "Tests failed:  $testfail"

exit $testfail
