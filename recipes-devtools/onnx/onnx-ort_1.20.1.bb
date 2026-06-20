require onnx.inc

SUMMARY = "ONNX built as a private static library for onnxruntime (isolated schema registry)"

inherit cmake

ONNX_ORT_PREFIX = "${prefix}/onnx-ort"
SYSROOT_DIRS += "${ONNX_ORT_PREFIX}"

EXTRA_OECMAKE = " \
    -DCMAKE_INSTALL_PREFIX=${ONNX_ORT_PREFIX} \
    -DBUILD_SHARED_LIBS=OFF \
    -DCMAKE_POSITION_INDEPENDENT_CODE=ON \
    -DONNX_DISABLE_STATIC_REGISTRATION=ON \
    -DONNX_USE_LITE_PROTO=ON \
    -DONNX_BUILD_TESTS=OFF \
    -DONNX_BUILD_BENCHMARKS=OFF \
    -DONNX_BUILD_PYTHON=OFF \
    -DONNX_VERIFY_PROTO3=ON \
    -DONNX_USE_PROTOBUF_SHARED_LIBS=ON \
    -DProtobuf_LIBRARY=${STAGING_LIBDIR}/libprotobuf-lite.so \
"

do_install() {
    cmake_do_install
    # remove tmpdir refs, harmful for downstream builds
    sed -i '/CMAKE_PREFIX_PATH/d' ${D}${ONNX_ORT_PREFIX}/lib/cmake/ONNX/ONNXConfig.cmake
    sed -i '/Protobuf_INCLUDE_DIR/d' ${D}${ONNX_ORT_PREFIX}/lib/cmake/ONNX/ONNXConfig.cmake
}

ALLOW_EMPTY:${PN} = "1"

FILES:${PN}-dev += " \
    ${ONNX_ORT_PREFIX}/include \
    ${ONNX_ORT_PREFIX}/lib/cmake \
"
FILES:${PN}-staticdev += "${ONNX_ORT_PREFIX}/lib/libonnx*.a"

INSANE_SKIP:${PN}-dev = "buildpaths"
INSANE_SKIP:${PN}-staticdev = "buildpaths"
