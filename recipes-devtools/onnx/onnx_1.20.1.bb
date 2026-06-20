require onnx.inc

inherit cmake python_setuptools_build_meta

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
