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
FASTER_RCNN_MODELS="faster_rcnn_models.tgz"
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

run_faster-rcnn() {
    local FASTER_RCNN_MODELS_URL="https://dl.dropboxusercontent.com/s/o6ii098bu51d139/${FASTER_RCNN_MODELS}"
    if ! wget --no-check-certificate -q ${FASTER_RCNN_MODELS_URL} -O ${SAMPLEDATA}/faster-rcnn/${FASTER_RCNN_MODELS}; then
        echo "Skipping Faster RCNN - Could not download ${FASTER_RCNN_MODELS}"
        return $SKIPCODE
    fi

    if [ -f "${SAMPLEDATA}/faster-rcnn/${FASTER_RCNN_MODELS}" ]; then
        tar zxf ${SAMPLEDATA}/faster-rcnn/${FASTER_RCNN_MODELS} -C ${SAMPLEDATA}/faster-rcnn --strip-components=1 --exclude=ZF_*
    fi

    echo "Running faster-rcnn"
    sample_fasterRCNN --datadir=${SAMPLEDATA}/faster-rcnn/
}

run_googlenet() {
    echo "Running googlenet"
    sample_googlenet --datadir=${SAMPLEDATA}/googlenet/
}

run_int8() {
    copy_mnist_data
    local TRAIN_IMAGES="train-images-idx3-ubyte"
    local TRAIN_LABELS="train-labels-idx1-ubyte"
    local TRAIN_IMAGES_URL="http://yann.lecun.com/exdb/mnist/${TRAIN_IMAGES}.gz"
    local TRAIN_LABELS_URL="http://yann.lecun.com/exdb/mnist/${TRAIN_LABELS}.gz"
    if ! wget --no-check-certificate -q ${TRAIN_IMAGES_URL} -O - | zcat > ${SAMPLEDATA}/mnist/${TRAIN_IMAGES}; then
        echo "Skipping INT8 - Could not download ${TRAIN_IMAGES}"
        return $SKIPCODE
    fi

    if ! wget --no-check-certificate -q ${TRAIN_LABELS_URL} -O - | zcat > ${SAMPLEDATA}/mnist/${TRAIN_LABELS}; then
        echo "Skipping INT8 - Could not download ${TRAIN_LABELS}"
        return $SKIPCODE
    fi

    echo "Running int8"
    sample_int8 --datadir=${SAMPLEDATA}/mnist/
}

run_int8_api() {
    copy_resnet50_data
    echo "Running int8-api"
    sample_int8_api --model=${SAMPLEDATA}/resnet50/ResNet50.onnx \
      --image=${SAMPLEDATA}/resnet50/airliner.ppm \
      --reference=${SAMPLEDATA}/int8_api/reference_labels.txt \
      --ranges=${SAMPLEDATA}/int8_api/resnet50_per_tensor_dynamic_range.txt
}

run_mnist() {
    echo "Running mnist"
    sample_mnist --datadir=${SAMPLEDATA}/mnist/
}

run_mnist_api() {
    copy_mnist_data
    sample_mnist_api --datadir=${SAMPLEDATA}/mnist/
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

run_uff_mnist() {
    copy_mnist_data
    echo "Running uff-mnist"
    sample_uff_mnist --datadir=${SAMPLEDATA}/mnist/
}

run_uff_plugin_v2_ext() {
    copy_mnist_data
    echo "Running uff-plugin-v2-ext"
    sample_uff_plugin_v2_ext --datadir=${SAMPLEDATA}/mnist/
}

run_trtexec(){
    if [ ! -x "${SAMPLEROOT}/bin/trtexec" ]; then
        echo "Skipping trtexec"
        return $SKIPCODE
    fi
    copy_mnist_data
    echo "Running trtexec: A Simple mnist model from Caffe"
    trtexec --deploy=${SAMPLEDATA}/mnist/mnist.prototxt \
      --model=${SAMPLEDATA}/mnist/mnist.caffemodel --output=prob \
      --batch=16 --saveEngine=mnist16.trt && \
	trtexec --loadEngine=mnist16.trt --batch=16
}

TESTS="algorithm_selector char-rnn dynamic_reshape faster-rcnn googlenet"
TESTS="$TESTS int8 int8_api mnist mnist_api onnx_mnist io_formats"
TESTS="$TESTS uff_mnist uff_plugin_v2_ext"
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
