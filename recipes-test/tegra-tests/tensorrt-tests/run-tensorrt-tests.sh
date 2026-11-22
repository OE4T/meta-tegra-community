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

run_dynamic_reshape() {
    copy_mnist_data
    echo "Running dynamic-reshape"
    sample_dynamic_reshape  --datadir=${SAMPLEDATA}/mnist/
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

run_progress_monitor() {
    copy_mnist_data
    echo "Running Progress Monitor"
    sample_progress_monitor --datadir=${SAMPLEDATA}/mnist/
}

run_editable_timing_cache() {
    echo "Running Editable Timing Cache"
    sample_editable_timing_cache
}

run_non_zero_plugin() {
    copy_mnist_data
    echo "Running Non Zero Plugin"
    sample_non_zero_plugin --datadir=${SAMPLEDATA}/mnist/
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
    trtexec --loadEngine=ResNet50.trt
}

run_trtexec_mnist(){
    if [ ! -x "${SAMPLEROOT}/bin/trtexec" ]; then
        echo "Skipping trtexec_mnist"
        return $SKIPCODE
    fi
    copy_mnist_data
    echo "Running trtexec: Building an MNIST engine with a 64 MiB workspace"
    trtexec --onnx=${SAMPLEDATA}/mnist/mnist.onnx \
      --saveEngine=mnist.plan --memPoolSize=workspace:64M && \
    trtexec --loadEngine=mnist.plan
}

have_dla() {
    if [ ! -e /dev/nvhost-ctrl-nvdla$1 ]; then
        echo "Skipping: no DLA core $1"
        return 1
    fi
}

trtexec_dla() {
    have_dla $1 || return $SKIPCODE
    if [ ! -x "${SAMPLEROOT}/bin/trtexec" ]; then
        echo "Skipping trtexec_dla$1"
        return $SKIPCODE
    fi
    copy_mnist_data
    echo "Running trtexec: MNIST on DLA core $1 with GPU fallback"
    trtexec --onnx=${SAMPLEDATA}/mnist/mnist.onnx --useDLACore=$1 --allowGPUFallback
}

progress_monitor_dla() {
    have_dla $1 || return $SKIPCODE
    copy_mnist_data
    echo "Running Progress Monitor on DLA core $1"
    sample_progress_monitor --datadir=${SAMPLEDATA}/mnist/ --useDLACore=$1
}

run_trtexec_dla0() { trtexec_dla 0; }
run_trtexec_dla1() { trtexec_dla 1; }
run_progress_monitor_dla0() { progress_monitor_dla 0; }
run_progress_monitor_dla1() { progress_monitor_dla 1; }

run_cudla() {
    have_dla 0 || return $SKIPCODE
    if [ ! -x "${SAMPLEROOT}/bin/sample_cudla" ]; then
        echo "Skipping cudla"
        return $SKIPCODE
    fi
    echo "Running cuDLA"
    sample_cudla
}

TESTS="dynamic_reshape"
TESTS="$TESTS onnx_mnist io_formats"
TESTS="$TESTS progress_monitor editable_timing_cache non_zero_plugin"
TESTS="$TESTS trtexec trtexec_mnist"
TESTS="$TESTS trtexec_dla0 trtexec_dla1 progress_monitor_dla0 progress_monitor_dla1 cudla"

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
