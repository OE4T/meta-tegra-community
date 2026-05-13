SUMMARY = "Open standard for machine learning interoperability"
HOMEPAGE = "https://github.com/onnx/onnx"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=3b83ef96387f14655fc854ddc3c6bd57"

SRC_URI = " \
    git://github.com/onnx/onnx.git;protocol=https;branch=rel-${PV};tag=v${PV} \
    file://0001-Updates-for-OE-cross-builds.patch \
    file://0002-Find-Python-with-Development.Module-for-nanobind-in-.patch \
    file://0003-setup.py-support-OE-cross-builds-interpreter-sysroot.patch \
    file://0004-Serialize-gen_proto-invocations-to-avoid-parallel-ra.patch \
    file://0005-Avoid-embedding-proto-sources-in-libonnx-prevents-du.patch \
"
SRCREV = "d3f6b795aedb48eaecc881bf5e8f5dd6efbe25b3"

inherit cmake python_setuptools_build_meta

DEPENDS += " \
    protobuf \
    protobuf-native \
"

EXTRA_OECMAKE = " \
    -DBUILD_SHARED_LIBS=ON \
    -DONNX_BUILD_TESTS=OFF \
    -DONNX_BUILD_BENCHMARKS=OFF \
    -DONNX_VERIFY_PROTO3=ON \
    -DONNX_USE_PROTOBUF_SHARED_LIBS=ON \
    -DProtobuf_LIBRARY=${STAGING_LIBDIR}/libprotobuf.so \
    ${@bb.utils.contains('PACKAGECONFIG', 'python', '-DONNX_BUILD_PYTHON=ON -DPython3_EXECUTABLE=${PYTHON} -DPython_EXECUTABLE=${PYTHON}', '', d)} \
"

PACKAGECONFIG ?= "python"
PACKAGECONFIG[python] = ",,python3-nanobind-native python3-numpy-native python3-protobuf-native tsl-robin-map-native,python3-numpy python3-protobuf python3-typing-extensions python3-ml-dtypes"

do_configure() {
    cmake_do_configure
}

do_compile() {
    cmake_do_compile
    if "${@bb.utils.contains('PACKAGECONFIG', 'python', 'true', 'false', d)}"; then
        python_pep517_do_compile
    fi
}

do_install() {
    cmake_do_install
    if "${@bb.utils.contains('PACKAGECONFIG', 'python', 'true', 'false', d)}"; then
        python_pep517_do_install
    fi
    # remove tmpdir refs, harmful for downstream builds
    sed -i '/CMAKE_PREFIX_PATH/d' ${D}${libdir}/cmake/ONNX/ONNXConfig.cmake
    sed -i '/Protobuf_INCLUDE_DIR/d' ${D}${libdir}/cmake/ONNX/ONNXConfig.cmake
}

PACKAGES += "python3-${PN}"
FILES:${PN} = "${libdir}/libonnx*.so*"
FILES:${PN}-dev = " \
    ${includedir}/onnx \
    ${libdir}/cmake \
"
FILES:python3-${PN} = " \
    ${PYTHON_SITEPACKAGES_DIR} \
    ${bindir}/check-node \
    ${bindir}/check-model \
    ${bindir}/backend-test-tools \
"
RDEPENDS:python3-${PN} = "${PN}"

INSANE_SKIP:${PN} = "buildpaths dev-so already-stripped"
INSANE_SKIP:python3-${PN} = "buildpaths already-stripped"
INSANE_SKIP:${PN}-dev = "buildpaths"
INSANE_SKIP:${PN}-dbg = "already-stripped"
