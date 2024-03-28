#!/bin/bash
# SPDX-License-Identifier: MIT
# Copyright (c) 2021, OpenEmbedded for Tegra Project
#
# WARNING: make sure you have at least a couple of GiB of free
# space availabe, as some of these samples need to download models/data.
#
# Some of these tests work better if you use jetson_clocks to
# speed things up.

SAMPLEROOT="/usr/src/tensorrt"
PATH="$SAMPLEROOT/bin:$PATH"
SAMPLEDATA="$PWD/tensorrt-tests"
RESNET50_MODELS="resnet50.tar.gz"
SKIPCODE=97
HAVE_MNIST_PGMS=no

gdrive_download() {
    local URL="https://docs.google.com/uc?export=download&id=$1"
    local cfile=$(mktemp gddl.XXXXXX)
    wget --quiet --load-cookies "$cfile" "https://docs.google.com/uc?export=download&confirm=$(wget --quiet --save-cookies $cfile --keep-session-cookies --no-check-certificate $URL -O- | sed -rn 's/.*confirm=([0-9A-Za-z_]+).*/\1\n/p')&id=$1" -O "$2"
    rc=$?
    rm -f "$cfile"
    return $rc
}

copy_mnist_data() {
    rm -rf "${SAMPLEDATA}/mnist"
    cp -R "${SAMPLEROOT}"/data/mnist "${SAMPLEDATA}/"
}

copy_resnet50_data() {
    rm -rf "${SAMPLEDATA}/resnet50"
    cp -R "${SAMPLEROOT}"/data/resnet50 "${SAMPLEDATA}/"
}

run_algorithm_selector() {
    copy_mnist_data
    echo "Running algorithm-selector"
    sample_algorithm_selector --datadir=${SAMPLEDATA}/mnist/
}

run_char-rnn() {
    echo "Running char-rnn"
    sample_char_rnn --datadir=${SAMPLEDATA}/char-rnn/
}

run_dynamic_reshape() {
    echo "Running dynamic-reshape"
    sample_dynamic_reshape  --datadir=${SAMPLEDATA}/mnist/
}

run_int8_api() {
    copy_resnet50_data
    echo "Running int8-api"
    sample_int8_api --model=${SAMPLEDATA}/resnet50/ResNet50.onnx \
      --image=${SAMPLEDATA}/resnet50/airliner.ppm \
      --reference=${SAMPLEDATA}/int8_api/reference_labels.txt \
      --ranges=${SAMPLEDATA}/int8_api/resnet50_per_tensor_dynamic_range.txt
}

run_onnx_mnist() {
    copy_mnist_data
    sample_onnx_mnist --datadir=${SAMPLEDATA}/mnist/
}

run_io_formats() {
    copy_mnist_data
    echo "Running io-formats"
    sample_io_formats --datadir=${SAMPLEDATA}/mnist/
}

run_trtexec(){
    if [ ! -x "${SAMPLEROOT}/bin/trtexec" ]; then
        echo "Skipping trtexec"
        return $SKIPCODE
    fi
    copy_resnet50_data
    echo "Running trtexec: Running an ONNX model with full dimensions"
    trtexec --onnx=${SAMPLEDATA}/resnet50/ResNet50.onnx \
      --saveEngine=ResNet50.trt && \
	trtexec --loadEngine=ResNet50.trt --batch=1
}

TESTS="algorithm_selector char-rnn dynamic_reshape"
TESTS="$TESTS int8_api onnx_mnist io_formats"
TESTS="$TESTS trtexec"

find_test() {
    for t in $TESTS; do
    if [ "$t" = "$1" ]; then
        echo "$t"
        return
    fi
    done
}

rm -rf "$SAMPLEDATA" 2>/dev/null
if ! mkdir -p "$SAMPLEDATA"; then
    echo "Cannot create $SAMPLEDATA for test storage" >&2
    exit 1
fi
freespace=$(df -k "$SAMPLEDATA" | tail -n 1 | awk '{print $4}')
if [ -z "$freespace" ]; then
    freespace=0
fi
if [ $freespace -lt 1000000 ]; then
    echo "Insufficient free space (${freespace}K) on $SAMPLEDATA" >&2
    exit 1
fi

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
	rm -rf "$SAMPLEDATA/$t"
	if [ -d "$SAMPLEROOT/data/$t" ]; then
	    cp -R "$SAMPLEROOT/data/$t" "$SAMPLEDATA/"
	else
	    mkdir -p "$SAMPLEDATA/$t"
	fi
	oldwd="$PWD"
	cd "$SAMPLEDATA/$t"
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
	cd "$oldwd"
	rm -rf "$SAMPLEDATA/$t"
    fi
done

echo "Tests run:     $testcount"
echo "Tests passed:  $testpass"
echo "Tests skipped: $testskip"
echo "Tests failed:  $testfail"

rm -rf "$SAMPLEDATA"

exit $testfail
