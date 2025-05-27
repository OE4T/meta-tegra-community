SUMMARY = "Open standard for machine learning interoperability"
HOMEPAGE = "https://github.com/onnx/onnx"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=3b83ef96387f14655fc854ddc3c6bd57"

SRC_URI = " \
    git://github.com/onnx/onnx.git;protocol=https;branch=rel-${PV};tag=v${PV} \
    file://0001-Updates-for-OE-cross-builds.patch \
"
SRCREV = "b8baa8446686496da4cc8fda09f2b6fe65c2a02c"

inherit cmake

DEPENDS += " \
    protobuf \
    protobuf-native \
    python3-pybind11 \
"

EXTRA_OECMAKE = " \
    -DBUILD_SHARED_LIBS=ON \
    -DONNX_BUILD_TESTS=OFF \
    -DONNX_BUILD_BENCHMARKS=OFF \
    -DONNX_VERIFY_PROTO3=ON \
    -DONNX_USE_PROTOBUF_SHARED_LIBS=ON \
    -DProtobuf_LIBRARY="${STAGING_LIBDIR}/libprotobuf.so" \
    -DONNX_DISABLE_STATIC_REGISTRATION=ON \
"

INSANE_SKIP:${PN}-dev = "buildpaths"
