#!/bin/sh
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
SAMPLEDATA="$SAMPLEROOT/data"
FASTER_RCNN_MODELS="faster_rcnn_models.tgz"
TRAIN_IMAGES="train-images-idx3-ubyte.gz"
TRAIN_LABELS="train-labels-idx1-ubyte.gz"
RESNET50_MODELS="resnet50.tar.gz"
SKIPCODE=97

cleaning() {
    # Cleaning in faster-rcnn folder
    if [ -e "${SAMPLEDATA}/faster-rcnn/${FASTER_RCNN_MODELS}" ]; then
        rm -f "${SAMPLEDATA}/faster-rcnn/${FASTER_RCNN_MODELS}"
    fi
    if [ -e "${SAMPLEDATA}/faster-rcnn/VGG16_faster_rcnn_final.caffemodel" ]; then
        rm -f "${SAMPLEDATA}/faster-rcnn/VGG16_faster_rcnn_final.caffemodel"
    fi
    if [ -e "${SAMPLEDATA}/faster-rcnn/ZF_faster_rcnn_final.caffemodel" ]; then
        rm -f "${SAMPLEDATA}/faster-rcnn/ZF_faster_rcnn_final.caffemodel"
    fi
    # Cleaning in mnist folder
    if [ -e "${SAMPLEDATA}/mnist/${TRAIN_IMAGES%.*}" ]; then
        rm -f "${SAMPLEDATA}/mnist/${TRAIN_IMAGES%.*}"
    fi
    if [ -e "${SAMPLEDATA}/mnist/${TRAIN_LABELS%.*}" ]; then
        rm -f "${SAMPLEDATA}/mnist/${TRAIN_LABELS%.*}"
    fi
    # Cleaning in int8_api folder
    if [ -e "${SAMPLEDATA}/int8_api/${RESNET50_MODELS}" ]; then
        rm -f "${SAMPLEDATA}/int8_api/${RESNET50_MODELS}"
    fi
    if [ -d "${SAMPLEDATA}/int8_api/${RESNET50_MODELS%%.*}" ]; then
        rm -rf "${SAMPLEDATA}/int8_api/${RESNET50_MODELS%%.*}"
    fi
    if [ -e "${SAMPLEDATA}/int8_api/resnet50.onnx" ]; then
        rm -f "${SAMPLEDATA}/int8_api/resnet50.onnx"
    fi
    # Cleaning in mlp and mnist folders
    DIRS="mlp mnist"
    for dir in $DIRS; do
        for i in {0..9}; do
            if [ -e "${SAMPLEDATA}/$dir/$i.pgm" ]; then
                rm -f "${SAMPLEDATA}/$dir/$i.pgm"
            fi
        done
    done
}

run_algorithm_selector() {
    echo "Running algorithm-selector"
    sample_algorithm_selector --datadir=${SAMPLEDATA}/mnist/
}

run_char_rnn() {
    echo "Running char-rnn"
    sample_char_rnn --datadir=${SAMPLEDATA}/char-rnn/
}

run_dynamic_reshape() {
    echo "Running dynamic-reshape"
    sample_dynamic_reshape  --datadir=${SAMPLEDATA}/mnist/
}

run_faster_rcnn() {
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
    local TRAIN_IMAGES_URL="http://yann.lecun.com/exdb/mnist/${TRAIN_IMAGES}"
    local TRAIN_LABELS_URL="http://yann.lecun.com/exdb/mnist/${TRAIN_LABELS}"
    if ! wget --no-check-certificate -q ${TRAIN_IMAGES_URL} -O ${SAMPLEDATA}/mnist/${TRAIN_IMAGES}; then
        echo "Skipping INT8 - Could not download ${TRAIN_IMAGES}"
        return $SKIPCODE
    fi

    if ! wget --no-check-certificate -q ${TRAIN_LABELS_URL} -O ${SAMPLEDATA}/mnist/${TRAIN_LABELS}; then
        echo "Skipping INT8 - Could not download ${TRAIN_LABELS}"
        return $SKIPCODE
    fi

    if [ -f "${SAMPLEDATA}/mnist/${TRAIN_IMAGES}" -a -f "${SAMPLEDATA}/mnist/${TRAIN_LABELS}" ] ; then
        gunzip ${SAMPLEDATA}/mnist/${TRAIN_IMAGES}
        gunzip ${SAMPLEDATA}/mnist/${TRAIN_LABELS}
    fi

    echo "Running int8"
    sample_int8 --datadir=${SAMPLEDATA}/mnist/
}

run_int8_api() {
    local RESNET50_MODELS_URL="https://s3.amazonaws.com/download.onnx/models/opset_9/${RESNET50_MODELS}"
    if ! wget --no-check-certificate -q ${RESNET50_MODELS_URL} -O ${SAMPLEDATA}/int8_api/${RESNET50_MODELS}; then
        echo "Skipping INT8 API - Could not download ${RESNET50_MODELS}"
        return $SKIPCODE
    fi

    if [ -f "${SAMPLEDATA}/int8_api/${RESNET50_MODELS}" ] ; then
        tar zxf "${SAMPLEDATA}/int8_api/${RESNET50_MODELS}" -C ${SAMPLEDATA}/int8_api
        mv ${SAMPLEDATA}/int8_api/resnet50/model.onnx ${SAMPLEDATA}/int8_api/resnet50.onnx
    fi

    echo "Running int8-api"
    sample_int8_api --model=${SAMPLEDATA}/resnet50/ResNet50.onnx \
      --image=${SAMPLEDATA}/int8_api/airliner.ppm \
      --reference=${SAMPLEDATA}/int8_api/reference_labels.txt \
      --ranges=${SAMPLEDATA}/int8_api/resnet50_per_tensor_dynamic_range.txt
}

run_mlp() {
    if [ -f "${SAMPLEDATA}/mnist/download_pgms.py" ]; then
        python3 "${SAMPLEDATA}/mnist/download_pgms.py" -o "${SAMPLEDATA}/mlp/"
    else
        echo "Skipping mlp - could nout find download_pgms.py in ${SAMPLEDATA}/mnist"
        return $SKIPCODE
    fi
    echo "Running multilayer-perceptron (MLP)"
    sample_mlp --datadir=${SAMPLEDATA}/mlp/
}

run_mnist() {
    echo "Running mnist"
    sample_mnist --datadir=${SAMPLEDATA}/mnist/
}

run_mnist_api() {
    echo "Running mnist-api"
    sample_mnist_api --datadir=${SAMPLEDATA}/mnist/
}

run_onnx_mnist() {
    echo "Running onnx-mnist"
    sample_onnx_mnist --datadir=${SAMPLEDATA}/mnist/
}

run_reformat_free_io() {
    echo "Running reformat-free-io"
    sample_reformat_free_io --datadir=${SAMPLEDATA}/mnist/
}

run_ssd() {
    if [ ! -f "${SAMPLEDATA}/ssd/ssd.prototxt" ]; then
        echo "Skipping ssd -  Could not find ssd.prototxt in ${SAMPLEDATA}"
        return $SKIPCODE
    fi

    echo "Running ssd"
    sample_ssd --datadir=${SAMPLEDATA}/ssd/
}

run_uff_faster_rcnn() {
    if [ ! -f "${SAMPLEDATA}/faster-rcnn/faster_rcnn.uff" ]; then
        echo "Skipping uff-faster-rcnn - Could not find faster_rcnn.uff in ${SAMPLEDATA}"
        return $SKIPCODE
    fi

    echo "Running uff-faster-rcnn"
    sample_uff_faster_rcnn --datadir=${SAMPLEDATA}/faster-rcnn/ -W 480 -H 272 -I 000456.ppm
}

run_uff_mask_rcnn() {
    if [ ! -d "${SAMPLEDATA}/maskrcnn" ]; then
        echo "Skipping uff-mask-rcnn - Could not find maskrcnn folder in ${SAMPLEDATA}"
        return $SKIPCODE
    fi

    echo "Running uff-mask-rcnn"
    sample_uff_mask_rcnn --datadir=${SAMPLEDATA}/maskrcnn
}

run_uff_mnist() {
    echo "Running uff-mnist"
    sample_uff_mnist --datadir=${SAMPLEDATA}/mnist/
}

run_uff_plugin_v2_ext() {
    echo "Running uff-plugin-v2-ext"
    sample_uff_plugin_v2_ext --datadir=${SAMPLEDATA}/mnist/
}

run_uff_ssd(){
    if [ ! -f "${SAMPLEDATA}/ssd/sample_ssd_relu6.uff" ]; then
        echo "Skipping uff-ssd - Could not find sample_ssd_relu6.uff in ${SAMPLEDATA}"
        return $SKIPCODE
    fi

    echo "Running uff-ssd"
    sample_uff_ssd --datadir=${SAMPLEDATA}/ssd/
}

run_trtexec_mnist_caffe(){
    if [ ! -x "${SAMPLEROOT}/bin/trtexec" ]; then
        echo "Skipping trtexec"
        return $SKIPCODE
    fi
    echo "Running trtexec: A Simple mnist model from Caffe"
    trtexec --deploy=${SAMPLEDATA}/mnist/mnist.prototxt \
      --model=${SAMPLEDATA}/mnist/mnist.caffemodel --output=prob \
      --batch=16 --saveEngine=mnist16.trt 
}

run_trtexec_onnx(){
    if [ ! -x "${SAMPLEROOT}/bin/trtexec" ]; then
        echo "Skipping trtexec"
        return $SKIPCODE
    fi
    echo "Running trtexec: An onnx model with full dimensions and dynamic shapes"
    trtexec --onnx=${SAMPLEDATA}/mnist/mnist.onnx
}

run_trtexec_tune_throughput(){
    if [ ! -x "${SAMPLEROOT}/bin/trtexec" ]; then
        echo "Skipping trtexec"
        return $SKIPCODE
    fi
    echo "Running trtexec: Tune throughput with multi-streaming"
    trtexec --deploy=${SAMPLEDATA}/googlenet/googlenet.prototxt --output=prob \
      --batch=1 --saveEngine=g1.trt --int8 --buildOnly
}

# Download pgms files before running TRT samples.
if [ -f "${SAMPLEDATA}/mnist/download_pgms.py" ]; then
    if ! python3 "${SAMPLEDATA}/mnist/download_pgms.py" -o "${SAMPLEDATA}/mnist"; then
        echo "ERR: could not download pgms files"
        cleaning
        exit 1
    fi
else
    echo "ERR: could not find download_pgms.py in ${SAMPLEDATA}/mnist"
    exit 1
fi

TESTS="algorithm_selector char_rnn dynamic_reshape faster_rcnn googlenet"
TESTS="$TESTS int8 int8_api mlp mnist mnist_api onnx_mnist reformat_free_io"
TESTS="$TESTS ssd uff_faster_rcnn uff_mask_rcnn uff_mnist uff_plugin_v2_ext"
TESTS="$TESTS uff_ssd trtexec_mnist_caffe trtexec_onnx trtexec_tune_throughput"

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
cleaning
exit $testfail
