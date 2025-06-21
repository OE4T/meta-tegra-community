DESCRIPTION = "Triton Python, C++ and Java client libraries, and GRPC-generated client examples for go, java and scala."
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=1c4cd58940438cc34ebeb4fec0a79ec8"
SECTION = "libs"

SRC_URI = "\
    git://github.com/triton-inference-server/client.git;protocol=https;branch=r22.05 \
    file://0001-Build-fixups.patch \
"

SRCREV = "1a167ab9eb1c6ffc23d12c04bc9ffbd3dede0856"

DEPENDS = "\
    triton-common \
    triton-core \
"

COMPATIBLE_MACHINE = "(cuda)"

inherit pkgconfig cmake python3-dir cuda


EXTRA_OECMAKE += '\
    -DCMAKE_INSTALL_PREFIX="${D}${prefix}" \
'

PACKAGECONFIG ??= "cc_http gpu python_http"
PACKAGECONFIG[cc_http] = "-DTRITON_ENABLE_CC_HTTP=ON,-DTRITON_ENABLE_CC_HTTP=OFF,curl"
PACKAGECONFIG[cc_grpc] = "-DTRITON_ENABLE_CC_GRPC=ON,-DTRITON_ENABLE_CC_GRPC=OFF,grpc protobuf protobuf-native"
PACKAGECONFIG[python_http] = "-DTRITON_ENABLE_PYTHON_HTTP=ON,-DTRITON_ENABLE_PYTHON_HTTP=OFF,,triton-python-backend"
PACKAGECONFIG[python_grpc] = "-DTRITON_ENABLE_PYTHON_GRPC=ON,-DTRITON_ENABLE_PYTHON_GRPC=OFF,,triton-python-backend"
PACKAGECONFIG[java] = "-DTRITON_ENABLE_JAVA_HTTP=ON,-DTRITON_ENABLE_JAVA_HTTP=OFF"
PACKAGECONFIG[perf] = "-DTRITON_ENABLE_PERF_ANALYZER=ON,-DTRITON_ENABLE_PERF_ANALYZER=OFF"
PACKAGECONFIG[perf_c] = "-DTRITON_ENABLE_PERF_ANALYZER_C_API=ON,-DTRITON_ENABLE_PERF_ANALYZER_C_API=OFF"
PACKAGECONFIG[perf_tfs] = "-DTRITON_ENABLE_PERF_ANALYZER_TFS=ON,-DTRITON_ENABLE_PERF_ANALYZER_TFS=OFF"
PACKAGECONFIG[perf_ts] = "-DTRITON_ENABLE_PERF_ANALYZER_TS=ON,-DTRITON_ENABLE_PERF_ANALYZER_TS=OFF"
PACKAGECONFIG[gpu] = "-DTRITON_ENABLE_GPU=ON,-DTRITON_ENABLE_GPU=OFF,cuda-cudart"
PACKAGECONFIG[tests] = "-DTRITON_ENABLE_TESTS=ON,-DTRITON_ENABLE_TESTS=OFF"
PACKAGECONFIG[examples] = "-DTRITON_ENABLE_EXAMPLES=ON,-DTRITON_ENABLE_EXAMPLES=OFF"

def get_epoch_time(d):
    import time
    return int(time.time())

SOURCE_DATE_EPOCH = "${@get_epoch_time(d)}"

cmake_runcmake_install() {
	bbnote ${DESTDIR:+DESTDIR=${DESTDIR} }${CMAKE_VERBOSE} cmake --build '${B}/cc-clients' "$@" -- ${EXTRA_OECMAKE_BUILD}
	eval ${DESTDIR:+DESTDIR=${DESTDIR} }${CMAKE_VERBOSE} cmake --build '${B}/cc-clients' "$@" -- ${EXTRA_OECMAKE_BUILD}
}

do_install() {
    cmake_runcmake_install --target ${OECMAKE_TARGET_INSTALL}
    install -d ${D}${PYTHON_SITEPACKAGES_DIR}
    cp --preserve=mode,timestamps -R ${B}/python-clients/library/linux/wheel/build/lib/* ${D}${PYTHON_SITEPACKAGES_DIR}
}

FILES:${PN} += " \
    ${includedir}/triton \
    ${PYTHON_SITEPACKAGES_DIR} \
"

SOLIBS = ".so"
FILES_SOLIBSDEV = ""
