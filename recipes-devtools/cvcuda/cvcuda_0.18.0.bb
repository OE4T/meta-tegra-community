SUMMARY = "CUDA-accelerated computer vision and image processing library"
HOMEPAGE = "https://github.com/CVCUDA/CV-CUDA"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE.md;md5=e31c40583d4626375468badb4d6cef38"

SRC_URI = " \
    git://github.com/CVCUDA/CV-CUDA.git;protocol=https;nobranch=1;tag=v${PV};lfs=0 \
    file://0001-tests-Skip-the-standalone-NVCV-tests-when-cross-compi.patch \
    file://0002-python-Make-the-binding-build-work-when-cross-compili.patch \
    file://0003-python-Let-the-packager-choose-where-the-module-is-in.patch \
    file://0004-tests-Skip-the-CUDA-architecture-policy-test-when-cro.patch \
    file://0005-python-Include-tensor-strides-in-the-Python-tensor-ca.patch \
    file://0006-Reject-zero-width-or-height-in-resize-instead-of-laun.patch \
    file://0007-python-Return-the-stream-from-Stream.__enter__.patch \
    file://0008-tests-Give-the-hue-channel-its-own-tolerance.patch \
    file://0009-python-Accept-pybind11-3.1.patch \
    file://run-ptest \
"
SRCREV = "b051f32d3e17669c456d588103f820e78af35832"

COMPATIBLE_MACHINE = "(cuda)"

inherit cmake cuda python3-dir python3targetconfig ptest

DEPENDS += "cuda-nvtx cuda-culibos python3-pybind11 dlpack"
DEPENDS += "${@bb.utils.contains('PTEST_ENABLED', '1', 'googletest openssl zlib', '', d)}"

EXTRA_OECMAKE += " \
    -DCMAKE_BUILD_TYPE=Release \
    -DBUILD_LIB=ON \
    -DBUILD_TESTS=OFF \
    -DBUILD_TESTS_CPP=${@bb.utils.contains('PTEST_ENABLED', '1', 'ON', 'OFF', d)} \
    -DOPENSSL_USE_STATIC_LIBS=OFF \
    -DBUILD_PYTHON=ON \
    -DPYTHON_VERSIONS=${PYTHON_BASEVERSION} \
    -DPYTHON_EXECUTABLE=${PYTHON} \
    -DPython_EXECUTABLE=${PYTHON} \
    -DPython_INCLUDE_DIR=${STAGING_INCDIR}/python${PYTHON_BASEVERSION} \
    -DCVCUDA_PYTHON_INSTALL_DIR=${PYTHON_SITEPACKAGES_DIR} \
    -DCMAKE_INSTALL_RPATH=${prefix}/local/cuda-${CUDA_VERSION}/${baselib} \
    -DBUILD_PYTHON_WHEEL=OFF \
    -DBUILD_BENCH=OFF \
    -DBUILD_SAMPLES=OFF \
    -DBUILD_DOCS=OFF \
"

CVCUDA_TEST_SUITES = "nvcv_test_* cvcuda_test_* run_tests.sh"

do_install:append() {
    rm -rf ${D}${prefix}/doc ${D}${prefix}/etc

    # No -ptest package to carry the suites, and they do not belong in ${bindir}.
    if [ "${PTEST_ENABLED}" != "1" ]; then
        for suite in ${CVCUDA_TEST_SUITES}; do
            rm -f ${D}${bindir}/$suite
        done
        rmdir ${D}${bindir} 2>/dev/null || true
    fi
}

do_install_ptest() {
    # BUILD_TESTS_CPP installs the suites and upstream's run_tests.sh driver into
    # ${bindir}; move them where ptest-runner looks. run_tests.sh keeps working
    # there, since it resolves the suites relative to its own directory.
    for suite in ${CVCUDA_TEST_SUITES}; do
        mv ${D}${bindir}/$suite ${D}${PTEST_PATH}/
    done

    # rmdir declines if a later version installs a real tool there.
    rmdir ${D}${bindir} 2>/dev/null || true
}

PACKAGE_BEFORE_PN += "python3-${PN}"
RDEPENDS:${PN}-ptest += "bash"

FILES:python3-${PN} = "${PYTHON_SITEPACKAGES_DIR}"
RDEPENDS:python3-${PN} += "${PN} python3-core python3-numpy"

INSANE_SKIP:${PN} += "already-stripped"
