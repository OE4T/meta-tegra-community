#!/bin/sh
# SPDX-License-Identifier: MIT
# Copyright (c) 2021-2026, OpenEmbedded for Tegra Project
#
# Usage: run-mmapi-tests [test-name ...] (default: all tests).
# Allow several GiB free in the working directory and TMPDIR (default /tmp).
# The runner saves clock settings, enables jetson_clocks, and restores on exit.
# Set DISPLAY for EGL rendering.
#
# SIPL requires MMAPI_SIPL_PLATFORM (-c). Optional: MMAPI_SIPL_JSON (-json)
# and MMAPI_SIPL_LINK_MASK (-m; quote space-separated masks).

SAMPLEROOT="/opt/tegra-mmapi"
PATH="$SAMPLEROOT/bin:$PATH"
SAMPLEVID="$SAMPLEROOT/data/Video/sample_outdoor_car_1080p_10fps.h264"
SAMPLEYUV="samplevid.yuv"
SAMPLEJPG="$SAMPLEROOT/data/Picture/nvidia-logo.jpg"
SAMPLEYUVPIC="nvidia-logo.yuv"
SAMPLEMODELS="$SAMPLEROOT/data/Model"
SKIPCODE=97
has_hw_encoder=

skip_test() {
    echo "Skipping: $*"
    return $SKIPCODE
}

require_encoder() {
    [ -n "$has_hw_encoder" ] || skip_test "no hardware video encoder"
}

require_display() {
    [ -n "$DISPLAY" ] || skip_test "DISPLAY is not set"
}

require_trt_sample() {
    [ -x "$SAMPLEROOT/bin/$1" ] || {
        skip_test "$1 was not built (requires objdetect)"
        return $?
    }
}

require_argus_cameras() {
    local count=$(find_argus_cameras | wc -l)
    [ "$count" -ge "$1" ] || {
        skip_test "requires $1 CSI camera(s), found $count"
        return $?
    }
}

# Keep generated data isolated, including fixed filenames used by samples.
with_workdir() (
    workdir=$(mktemp -d "${TMPDIR:-/tmp}/mmapi-test.XXXXXX") || exit 1
    trap 'rm -rf "$workdir"' EXIT
    trap 'exit 1' HUP INT TERM
    cd "$workdir" || exit 1
    "$@"
)

bounded() {
    timeout --kill-after=5 "$@"
}

check_size() {
    [ -f "$1" ] && [ "$(wc -c < "$1")" -eq "$2" ] || {
        echo "Error: $1 must contain $2 bytes"
        return 1
    }
}

make_encode_input() {
    # Ten 640x480 planar YUV420 frames, independent of camera hardware.
    dd if=/dev/zero of=input.yuv bs=460800 count=10 || return $?
    check_size input.yuv 4608000
}

check_decoded_frames() {
    local codec=$1 stream=$2 output=$3
    local width=$4 height=$5 frames=$6
    bounded 120 video_decode "$codec" --disable-rendering -f 2 -o "$output" "$stream" || return $?
    check_size "$output" "$((width * height * 3 / 2 * frames))"
}

make_trt_engine() {
    bounded 300 /usr/src/tensorrt/bin/trtexec \
        --onnx="$SAMPLEMODELS/resnet10/resnet10_dynamic_batch.onnx" \
        --minShapes=data:1x3x368x640 --optShapes=data:2x3x368x640 \
        --maxShapes=data:4x3x368x640 --fp16 \
        --saveEngine=resnet10.engine --skipInference || return $?
    [ -s resnet10.engine ]
}

run_video_decode() {
    vid="${1:-$SAMPLEVID}"
    echo "Running 00_video_decode on $vid"
    video_decode H264 "$vid"
}

generate_yuv_sample_vid() {
    echo "Converting sample video to YUV, please wait"
    # video_dec_cuda creates the output file before decoding, so an
    # interrupted or failed run leaves an empty file behind. Remove any
    # stale copy first and discard the output if the decode fails, so we
    # never cache a truncated/empty YUV that later poisons the encoders.
    rm -f "$SAMPLEYUV"
    if ! video_dec_cuda "$SAMPLEVID" H264 --disable-rendering -o "$SAMPLEYUV" -f 2; then
	echo "Error: failed to generate $SAMPLEYUV"
	rm -f "$SAMPLEYUV"
	return 1
    fi
}

run_video_encode() {
    local rc=0
    require_encoder || return $?
    [ -s "$SAMPLEYUV" ] || generate_yuv_sample_vid || return 1
    echo "Running 01_video_encode"
    rm -f samplevid.h264
    video_encode "$SAMPLEYUV" 1920 1080 H264 samplevid.h264 || rc=1
    [ $rc -ne 0 ] || run_video_decode samplevid.h264 || rc=1
    rm -f samplevid.h264
    return $rc
}

run_video_dec_cuda() {
    vid="${1:-$SAMPLEVID}"
    echo "Running 02_video_dec_cuda $vid"
    if [ -n "$2" ]; then
	video_dec_cuda "$vid" H264 --bbox-file "$2"
    else
	video_dec_cuda "$vid" H264
    fi
}

run_video_cuda_enc() {
    local rc=0
    require_encoder || return $?
    [ -s "$SAMPLEYUV" ] || generate_yuv_sample_vid || return 1
    echo "Running 03_video_cuda_enc"
    rm -f samplevid.h264
    video_cuda_enc "$SAMPLEYUV" 1920 1080 H264 samplevid.h264 || rc=1
    [ $rc -ne 0 ] || run_video_dec_cuda samplevid.h264 || rc=1
    rm -f samplevid.h264
    return $rc
}

run_video_dec_trt() {
    if [ ! -x "$SAMPLEROOT/bin/video_dec_trt" ]; then
	echo "Skipping 04_video_dec_trt"
	return $SKIPCODE
    fi
    local rc=0
    echo "Running 04_video_dec_trt"
    /usr/src/tensorrt/bin/trtexec --onnx="$SAMPLEMODELS/resnet10/resnet10_dynamic_batch.onnx" \
        --minShapes=data:1x3x368x640 \
        --optShapes=data:2x3x368x640 \
        --maxShapes=data:2x3x368x640 \
        --saveEngine=resnet10.trt
    video_dec_trt 2 "$SAMPLEVID" "$SAMPLEVID" H264 --trt-engine resnet10.trt || rc=1
    [ $rc -ne 0 ] || run_video_dec_cuda "$SAMPLEVID" "result0.txt" || rc=1
    [ $rc -ne 0 ] || run_video_dec_cuda "$SAMPLEVID" "result1.txt" || rc=1
    rm -f result0.txt result1.txt trtModel.cache
    return $rc
}

run_jpeg_encode() {
    local rc=0
    [ -s "$SAMPLEYUVPIC" ] || run_jpeg_decode || return 1
    echo "Running 05_jpeg_encode"
    jpeg_encode "$SAMPLEYUVPIC" 1920 1080 "sample-encoded.jpg" || rc=1
    rm -f sample-encoded.jpg
    return $rc
}

run_jpeg_decode() {
    jpeg="${1:-$SAMPLEJPG}"
    yuv="${2:-$SAMPLEYUVPIC}"
    echo "Running 06_jpeg_decode"
    jpeg_decode num_files 1 "$jpeg" "$yuv"
}

run_video_convert() {
    local rc=0
    [ -s "$SAMPLEYUVPIC" ] || run_jpeg_decode || return 1
    video_convert "$SAMPLEYUVPIC" 1920 1080 YUV420 sample.yuyv 1920 1080 YUYV || rc=1
    rm -f sample.yuyv
    return $rc
}

run_video_dec_drm() {
    if [ -n "$DISPLAY" ]; then
	echo "Cannot run 08_video_dec_drm when running from X, skipping"
	return $SKIPCODE
    fi
    local rc=0
    for i in 0 1 2; do
	if [ -e /sys/class/graphics/fb${i} ]; then
	    echo "4" > /sys/class/graphics/fb${i}/blank
	    echo "0x0" > /sys/class/graphics/fb${i}/device/win_mask
	fi
    done
    echo "0x3f" > /sys/class/graphics/fb0/device/win_mask
    echo "0" > /sys/class/graphics/fb0/blank
    xstatus=$(systemctl is-active xserver-nodm)
    if [ "$xstatus" = "active" ]; then
	systemctl stop xserver-nodm
    fi
    echo "Running 08_video_dec_drm"
    video_dec_drm "$SAMPLEVID" H264 || rc=1
    if [ "$xstatus" = "active" ]; then
	systemctl start xserver-nodm
    fi
    return $rc
}

run_argus_camera_jpeg() {
    require_argus_cameras 1 || return $?
    local rc=0
    echo "Running 09_argus_camera_jpeg"
    argus_camera_jpeg || rc=1
    rm -f output*.jpg
    return $rc
}

run_argus_camera_recording() {
    require_argus_cameras 1 || return $?
    require_encoder || return $?
    local rc=0
    echo "Running 10_argus_camera_recording"
    argus_camera_recording || rc=1
    [ $rc -ne 0 ] || run_video_decode output.h264 || rc=1
    rm -f output.h264
    return $rc
}

find_usb_camera() {
    for d in /dev/video*; do
	[ -c "$d" ] || continue
	if readlink -f /sys/class/video4linux/$(basename $d) | grep -q usb; then
	    echo "$d"
	    return
	fi
    done
    echo ""
}

find_argus_cameras() {
    for d in /dev/video*; do
        [ -c "$d" ] || continue
        if readlink -f /sys/class/video4linux/$(basename "$d") | grep -q tegra-capture-vi; then
            echo "$d"
        fi
    done
}

find_argus_camera() {
    find_argus_cameras | head -n 1
}

run_v4l2_camera_cuda() {
    local usbcam=$(find_usb_camera)
    if [ -z "$usbcam" ]; then
	echo "Skipping 12_v4l2_camera_cuda - no USB camera"
	return $SKIPCODE
    fi
    echo "Running 12_v4l2_camera_cuda -d $usbcam"
    v4l2_camera_cuda -d $usbcam -s 1280x720 -f YUYV -c -N 120
}

run_camera_sample() {
    require_argus_cameras 1 || return $?
    local mipicam=$(find_argus_camera)
    camera_sample -sid ${mipicam##/dev/video}
}

run_decode_sample() {
    local rc=0
    echo "Running decode_sample"
    decode_sample "$SAMPLEVID" ut-samplevid.yuv || rc=1
    rm -f ut-samplevid.yuv
    return $rc
}

run_encode_sample() {
    local rc=0
    require_encoder || return $?
    [ -s "$SAMPLEYUV" ] || generate_yuv_sample_vid || return 1
    echo "Running encode_sample"
    rm -f ut-samplevid.h264
    encode_sample "$SAMPLEYUV" 1920 1080 ut-samplevid.h264 || rc=1
    [ $rc -ne 0 ] || run_video_decode ut-samplevid.h264 || rc=1
    rm -f ut-samplevid.h264
    return $rc
}

run_transform_sample() {
    local rc=0
    [ -s "$SAMPLEYUV" ] || generate_yuv_sample_vid || return 1
    echo "Running transform_sample"
    transform_sample "$SAMPLEYUV" yuv420 1920 1080 nv12-sample.yuv nv12 || rc=1
    rm -f nv12-sample.yuv
    return $rc
}

run_v4l2_camera_cuda_rgb() {
    local usbcam=$(find_usb_camera)
    if [ -z "$usbcam" ]; then
	echo "Skipping 18_v4l2_camera_cuda_rgb - no USB camera"
	return $SKIPCODE
    fi
    echo "Running 18_v4l2_camera_cuda_rgb -d $usbcam"
    v4l2_camera_cuda_rgb -d $usbcam
    rm -f out.ppm
}

# Additional sample coverage. Camera and platform prerequisites are checked
# before creating work directories or building TensorRT engines.
run_video_osd() {
    with_workdir test_video_osd
}

test_video_osd() {
    bounded 120 jpeg_decode num_files 1 "$SAMPLEJPG" input.yuv || return $?
    bounded 120 video_convert input.yuv 1920 1080 YUV420 input.rgba 1920 1080 RGBA || return $?
    check_size input.rgba0 8294400 || return $?
    for mode in 0 1; do
        bounded 120 video_osd input.rgba0 1920 1080 RGBA "osd-$mode-" RGBA -m "$mode" || return $?
        check_size "osd-$mode-0" 8294400 || return $?
        if cmp -s input.rgba0 "osd-$mode-0"; then
            echo "Error: OSD mode $mode did not modify the image"
            return 1
        fi
    done
}

run_multivideo_decode() {
    with_workdir test_multivideo_decode
}

test_multivideo_decode() {
    # Decode the complete packaged clip in two concurrent instances. Its
    # reference frame count is established using the plain decoder.
    bounded 120 video_decode H264 --disable-rendering -f 2 -o reference.yuv "$SAMPLEVID" || return $?
    [ -s reference.yuv ] || return 1
    local size=$(wc -c < reference.yuv)
    [ "$((size % 3110400))" -eq 0 ] || return 1
    bounded 120 multivideo_decode num_files 2 \
        "$SAMPLEVID" H264 -o stream-a.yuv "$SAMPLEVID" H264 -o stream-b.yuv \
        --disable-rendering -f 2 || return $?
    check_size stream-a.yuv "$size" || return $?
    check_size stream-b.yuv "$size" || return $?
    # The multi decoder uses a different transform filter from video_decode.
    cmp stream-a.yuv stream-b.yuv
}

run_multivideo_encode() {
    require_encoder || return $?
    with_workdir test_multivideo_encode
}

test_multivideo_encode() {
    make_encode_input || return $?
    bounded 120 multivideo_encode num_files 2 \
        input.yuv 640 480 H264 stream-a.h264 \
        input.yuv 640 480 H264 stream-b.h264 || return $?
    for stream in a b; do
        check_decoded_frames H264 "stream-$stream.h264" "decoded-$stream.yuv" 640 480 10 || return $?
    done
}

run_multivideo_transcode() {
    require_encoder || return $?
    with_workdir test_multivideo_transcode
}

test_multivideo_transcode() {
    make_encode_input || return $?
    bounded 120 video_encode input.yuv 640 480 H264 input.h264 || return $?
    bounded 120 multivideo_transcode num_files 2 \
        input.h264 H264 stream-a.h265 H265 \
        input.h264 H264 stream-b.h265 H265 || return $?
    for stream in a b; do
        check_decoded_frames H265 "stream-$stream.h265" "decoded-$stream.yuv" 640 480 10 || return $?
    done
}

run_backend() {
    require_trt_sample backend || return $?
    require_display || return $?
    with_workdir test_backend
}

test_backend() {
    make_trt_engine || return $?
    bounded 180 backend 1 "$SAMPLEVID" H264 --trt-enginefile resnet10.engine \
        --trt-proc-interval 1 --trt-dumpresult 1 -fps 10 || return $?
    # Require inference results, not just successful engine loading.
    grep -q '^frame:' result.txt
}

run_frontend() {
    require_argus_cameras 1 || return $?
    require_encoder || return $?
    require_trt_sample frontend || return $?
    with_workdir test_frontend
}

test_frontend() {
    make_trt_engine || return $?
    local rc=0
    bounded 120 frontend --engine resnet10.engine --no-preview -v -t 5 > frontend.log 2>&1 || rc=$?
    cat frontend.log
    [ "$rc" -eq 0 ] || return "$rc"
    # The sample does not propagate every producer error to its exit code.
    grep -q 'TRT: Batch done' frontend.log || return 1
    for spec in '1 640 480' '2 1280 720' '3 1920 1080'; do
        set -- $spec
        bounded 120 video_decode H265 --disable-rendering -f 2 -o "decoded-$1.yuv" "output$1.h265" || return $?
        local size=$(wc -c < "decoded-$1.yuv")
        [ "$size" -gt 0 ] && [ "$((size % ($2 * $3 * 3 / 2)))" -eq 0 ] || return 1
    done
    bounded 120 video_decode H264 --disable-rendering -f 2 -o decoded-trt.yuv trt.h264 || return $?
    [ -s decoded-trt.yuv ]
}

run_argus_multi_camera() {
    require_argus_cameras 1 || return $?
    require_display || return $?
    with_workdir test_argus_multi_camera 1
}

run_argus_multi_camera_composite() {
    require_argus_cameras 2 || return $?
    require_display || return $?
    with_workdir test_argus_multi_camera 2
}

test_argus_multi_camera() {
    local rc=0
    bounded 120 argus_multi_camera -n "$1" -c 30 > camera.log 2>&1 || rc=$?
    cat camera.log
    [ "$rc" -eq 0 ] || return "$rc"
    # The sample caps its stream count at the number Argus enumerates.
    # Require every requested producer to connect before accepting capture.
    local connected=$(grep -c 'CONSUMER: Producer has connected; continuing.' camera.log)
    [ "$connected" -eq "$1" ] || {
        echo "Error: requested $1 Argus streams, connected $connected"
        return 1
    }
    [ "$(grep -c '^CONSUMER: Render frame ' camera.log)" -eq 30 ] || return 1
    grep -Fxq 'CONSUMER: Done.' camera.log
}

run_sipl_coe_sample() {
    [ -n "${MMAPI_SIPL_PLATFORM:-}" ] || {
        skip_test "no SIPL camera platform configured (MMAPI_SIPL_PLATFORM)"
        return $?
    }
    local json=
    if [ -n "${MMAPI_SIPL_JSON:-}" ]; then
        [ -r "$MMAPI_SIPL_JSON" ] || {
            echo "Error: cannot read SIPL JSON: $MMAPI_SIPL_JSON"
            return 1
        }
        json=$(readlink -f "$MMAPI_SIPL_JSON") || return $?
    fi
    with_workdir test_sipl_coe_sample "$json"
}

test_sipl_coe_sample() {
    local json=$1
    set -- -c "$MMAPI_SIPL_PLATFORM" -t 5 --disable-rendering -o capture.raw
    [ -z "$json" ] || set -- "$@" -json "$json"
    [ -z "${MMAPI_SIPL_LINK_MASK:-}" ] || set -- "$@" -m "$MMAPI_SIPL_LINK_MASK"
    bounded 120 sipl_coe_sample "$@" || return $?
    [ -s capture.raw ]
}

TESTS="video_decode video_encode video_dec_cuda video_cuda_enc video_dec_trt jpeg_encode"
TESTS="$TESTS jpeg_decode video_convert video_dec_drm argus_camera_jpeg argus_camera_recording"
TESTS="$TESTS v4l2_camera_cuda camera_sample decode_sample encode_sample transform_sample"
TESTS="$TESTS v4l2_camera_cuda_rgb video_osd multivideo_decode multivideo_encode"
TESTS="$TESTS multivideo_transcode backend frontend argus_multi_camera"
TESTS="$TESTS argus_multi_camera_composite sipl_coe_sample"

find_test() {
    for t in $TESTS; do
	if [ "$t" = "$1" ]; then
	    echo "$t"
	    return
	fi
    done
}

restore_clocks() {
    local rc=$?
    trap - EXIT HUP INT TERM
    if jetson_clocks --restore "$clock_state_dir/clocks.conf"; then
        rm -rf "$clock_state_dir"
    else
        echo "Error: failed to restore clocks; saved settings: $clock_state_dir/clocks.conf" >&2
        [ "$rc" -ne 0 ] || rc=1
    fi
    exit "$rc"
}

# Keep X rendering tests from being throttled while the display is asleep.
if [ -n "${DISPLAY:-}" ] && command -v xset >/dev/null 2>&1; then
    xset -dpms
    xset s off
    xset s noblank
fi

clock_state_dir=$(mktemp -d "${TMPDIR:-/tmp}/mmapi-clocks.XXXXXX") || exit 1
if ! jetson_clocks --store "$clock_state_dir/clocks.conf"; then
    rm -rf "$clock_state_dir"
    exit 1
fi
trap restore_clocks EXIT
trap 'exit 129' HUP
trap 'exit 130' INT
trap 'exit 143' TERM
jetson_clocks || exit 1

if [ -c /dev/v4l2-nvenc ]; then
    has_hw_encoder=yes
fi

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
