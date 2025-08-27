#!/bin/sh
# SPDX-License-Identifier: MIT
# Copyright (c) 2025, OpenEmbedded for Tegra Project

SAMPLEROOT="/usr/bin/cudnn-samples"
PATH="$SAMPLEROOT:$PATH"
SKIPCODE=97

run_conv_sample_test1() {
    echo "Running conv_sample test1"
    conv_sample "-c2048" "-h7" "-w7" "-k512" "-r1" "-s1" "-pad_h0" "-pad_w0" "-u1" "-v1"
}

run_conv_sample_test2() {
    echo "Running conv_sample test2"
    conv_sample "-c512" "-h28" "-w28" "-k128" "-r1" "-s1" "-pad_h0" "-pad_w0" "-u1" "-v1"
}

run_conv_sample_test3() {
    echo "Running conv_sample test3"
    conv_sample "-c512" "-h28" "-w28" "-k1024" "-r1" "-s1" "-pad_h0" "-pad_w0" "-u2" "-v2"
}

run_conv_sample_test4() {
    echo "Running conv_sample test4"
    conv_sample "-c512" "-h28" "-w28" "-k256" "-r1" "-s1" "-pad_h0" "-pad_w0" "-u2" "-v2"
}

run_conv_sample_test5() {
    echo "Running conv_sample test5"
    conv_sample "-c256" "-h14" "-w14" "-k256" "-r3" "-s3" "-pad_h1" "-pad_w1" "-u1" "-v1"
}

run_conv_sample_test6() {
    echo "Running conv_sample test6"
    conv_sample "-c256" "-h14" "-w14" "-k1024" "-r1" "-s1" "-pad_h0" "-pad_w0" "-u1" "-v1"
}

run_conv_sample_test7() {
    echo "Running conv_sample test7"
    conv_sample "-c1024" "-h14" "-w14" "-k256" "-r1" "-s1" "-pad_h0" "-pad_w0" "-u1" "-v1"
}

run_conv_sample_test8() {
    echo "Running conv_sample test8"
    conv_sample "-c1024" "-h14" "-w14" "-k2048" "-r1" "-s1" "-pad_h0" "-pad_w0" "-u2" "-v2"
}

run_conv_sample_test9() {
    echo "Running conv_sample test9"
    conv_sample "-c1024" "-h14" "-w14" "-k512" "-r1" "-s1" "-pad_h0" "-pad_w0" "-u2" "-v2"
}

run_conv_sample_test10() {
    echo "Running conv_sample test10"
    conv_sample "-c512" "-h7" "-w7" "-k512" "-r3" "-s3" "-pad_h1" "-pad_w1" "-u1" "-v1"
}

run_conv_sample_test11() {
    echo "Running conv_sample test11"
    conv_sample "-c512" "-h7" "-w7" "-k2048" "-r1" "-s1" "-pad_h0" "-pad_w0" "-u1" "-v1"
}

run_conv_sample_test12() {
    echo "Running conv_sample test12"
    conv_sample "-mathType1" "-filterFormat2" "-dataType2" "-n1" "-c512" "-h100" "-w100" "-k64" "-r8" "-s8" "-pad_h0" "-pad_w0" "-u1" "-v1" "-b"
}

run_conv_sample_test13() {
    echo "Running conv_sample test13"
    conv_sample "-mathType1" "-filterFormat2" "-dataType2" "-n1" "-c4096" "-h64" "-w64" "-k64" "-r4" "-s4" "-pad_h1" "-pad_w1" "-u1" "-v1" "-b"
}

run_conv_sample_test14() {
    echo "Running conv_sample test14"
    conv_sample "-mathType1" "-filterFormat2" "-dataType2" "-n1" "-c512" "-h100" "-w100" "-k64" "-r8" "-s8" "-pad_h1" "-pad_w1" "-u1" "-v1" "-b"
}

run_conv_sample_test15() {
    echo "Running conv_sample test15"
    conv_sample "-mathType1" "-filterFormat2" "-dataType2" "-n1" "-c512" "-h128" "-w128" "-k64" "-r13" "-s13" "-pad_h1" "-pad_w1" "-u1" "-v1" "-b"
}

run_conv_sample_test16() {
    echo "Running conv_sample test16"
    conv_sample "-mathType1" "-filterFormat2" "-dataType3" "-n1" "-c512" "-h100" "-w100" "-k64" "-r8" "-s8" "-pad_h0" "-pad_w0" "-u1" "-v1" "-b"
}

run_conv_sample_test17() {
    echo "Running conv_sample test17"
    conv_sample "-mathType1" "-filterFormat2" "-dataType3" "-n1" "-c4096" "-h64" "-w64" "-k64" "-r4" "-s4" "-pad_h1" "-pad_w1" "-u1" "-v1" "-b"
}

run_conv_sample_test18() {
    echo "Running conv_sample test18"
    conv_sample "-mathType1" "-filterFormat2" "-dataType3" "-n1" "-c512" "-h100" "-w100" "-k64" "-r8" "-s8" "-pad_h1" "-pad_w1" "-u1" "-v1" "-b"
}

run_conv_sample_test19() {
    echo "Running conv_sample test19"
    conv_sample "-mathType1" "-filterFormat2" "-dataType3" "-n1" "-c512" "-h128" "-w128" "-k64" "-r13" "-s13" "-pad_h1" "-pad_w1" "-u1" "-v1" "-b"
}

run_conv_sample_test20() {
    echo "Running conv_sample test20"
    conv_sample "-mathType1" "-filterFormat2" "-dataType3" "-n5" "-c32" "-h16" "-w16" "-k32" "-r5" "-s5" "-pad_h0" "-pad_w0" "-u1" "-v1" "-b" "-transformFromNCHW"
}

run_conv_sample_test21() {
    echo "Running conv_sample test21"
    conv_sample "-dgrad" "-c1024" "-h14" "-w14" "-k2048" "-r1" "-s1" "-pad_h0" "-pad_w0" "-u2" "-v2" "-fold"
}


run_mnistCUDNN() {
    echo "Running mnistCUDNN"
    CUDNN_DATA_PATH="$SAMPLEROOT/data" mnistCUDNN 
}

run_multiHeadAttention() {
    echo "Running multiHeadAttention"
    multiHeadAttention \
      "-attnFileDump1" \
      "-attnTrain1" \
      "-attnDataType0" \
      "-attnCompPrec0" \
      "-attnResLink1" \
      "-attnDataLayout3" \
      "-attnNumHeads3" \
      "-attnBeamSize1" \
      "-attnBatchSize1" \
      "-attnQsize8" \
      "-attnKsize8" \
      "-attnVsize8" \
      "-attnProjQsize2" \
      "-attnProjKsize2" \
      "-attnProjVsize2" \
      "-attnProjOsize8" \
      "-attnResLink0" \
      "-attnProjBias0" \
      "-attnSeqLenQ4" \
      "-attnSeqLenK10" \
      "-attnSmScaler1.0" \
      "-attnRandSeed1234" 
}

run_RNN_v8_0_test1() {
    echo "Running RNN_v8.0 test1"
    RNN_v8.0 \
      "-dataType1" \
      "-seqLength20" \
      "-numLayers2" \
      "-inputSize512" \
      "-hiddenSize512" \
      "-projSize512" \
      "-miniBatch64" \
      "-inputMode1" \
      "-dirMode0" \
      "-cellMode0" \
      "-biasMode3" \
      "-algorithm0" \
      "-mathPrecision1" \
      "-mathType0" \
      "-dropout0.0" \
      "-printWeights0"
}

run_RNN_v8_0_test2() {
    echo "Running RNN_v8.0 test2"
    RNN_v8.0 \
      "-dataType1" \
      "-seqLength20" \
      "-numLayers2" \
      "-inputSize512" \
      "-hiddenSize512" \
      "-projSize512" \
      "-miniBatch64" \
      "-inputMode1" \
      "-dirMode0" \
      "-cellMode1" \
      "-biasMode3" \
      "-algorithm0" \
      "-mathPrecision1" \
      "-mathType0" \
      "-dropout0.0" \
      "-printWeights0"
}

run_RNN_v8_0_test3() {
    echo "Running RNN_v8.0 test3"
    RNN_v8.0 \
      "-dataType1" \
      "-seqLength20" \
      "-numLayers2" \
      "-inputSize512" \
      "-hiddenSize512" \
      "-projSize512" \
      "-miniBatch64" \
      "-inputMode1" \
      "-dirMode0" \
      "-cellMode2" \
      "-biasMode3" \
      "-algorithm0" \
      "-mathPrecision1" \
      "-mathType0" \
      "-dropout0.0" \
      "-printWeights0"
}

run_RNN_v8_0_test4() {
    echo "Running RNN_v8.0 test4"
    RNN_v8.0 \
      "-dataType1" \
      "-seqLength20" \
      "-numLayers2" \
      "-inputSize512" \
      "-hiddenSize512" \
      "-projSize512" \
      "-miniBatch64" \
      "-inputMode1" \
      "-dirMode0" \
      "-cellMode3" \
      "-biasMode3" \
      "-algorithm0" \
      "-mathPrecision1" \
      "-mathType0" \
      "-dropout0.0" \
      "-printWeights0"
}

TESTS="conv_sample_test1 conv_sample_test2 conv_sample_test3 conv_sample_test4 conv_sample_test5"
TESTS="$TESTS conv_sample_test6 conv_sample_test7 conv_sample_test8 conv_sample_test9"
TESTS="$TESTS conv_sample_test10 conv_sample_test11 conv_sample_test12 conv_sample_test13"
TESTS="$TESTS conv_sample_test14 conv_sample_test15 conv_sample_test16 conv_sample_test17"
TESTS="$TESTS conv_sample_test18 conv_sample_test19 conv_sample_test20 conv_sample_test21"
TESTS="$TESTS mnistCUDNN multiHeadAttention RNN_v8_0_test1 RNN_v8_0_test2 RNN_v8_0_test3 RNN_v8_0_test4"

find_test() {
    for t in $TESTS; do
    if [ "$t" = "$1" ]; then
        echo "$t"
        return
    fi
    done
}

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

rm -rf "$SAMPLEDATA"

exit $testfail
