DESCRIPTION = "Triton Python, C++ and Java client libraries, and GRPC-generated client examples for go, java and scala."
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=1c4cd58940438cc34ebeb4fec0a79ec8"
SECTION = "libs"

SRC_URI = "\
    git://github.com/triton-inference-server/client.git;protocol=https;branch=r25.05 \
    file://0001-Build-fixups.patch \
"

SRCREV = "9cf06a1beec8b8cf4854247ba77704f03127f2fb"

DEPENDS = "\
    triton-common \
    triton-core \
"

COMPATIBLE_MACHINE = "(cuda)"

inherit pkgconfig cmake setuptools3 cuda

SETUPTOOLS_SETUP_PATH = "${S}/src/python/library"

EXTRA_OECMAKE += '\
    -DCMAKE_INSTALL_PREFIX="${D}${prefix}" \
    -DTRITON_VERSION="${PV}" \
'

PACKAGECONFIG ??= "cc_http gpu python_http"
PACKAGECONFIG[cc_http] = "-DTRITON_ENABLE_CC_HTTP=ON,-DTRITON_ENABLE_CC_HTTP=OFF,re2 curl"
PACKAGECONFIG[cc_grpc] = "-DTRITON_ENABLE_CC_GRPC=ON,-DTRITON_ENABLE_CC_GRPC=OFF,re2 rapidjson grpc protobuf protobuf-native"
PACKAGECONFIG[python_http] = "-DTRITON_ENABLE_PYTHON_HTTP=ON,-DTRITON_ENABLE_PYTHON_HTTP=OFF,,triton-python-backend"
PACKAGECONFIG[python_grpc] = "-DTRITON_ENABLE_PYTHON_GRPC=ON,-DTRITON_ENABLE_PYTHON_GRPC=OFF,,triton-python-backend"
PACKAGECONFIG[java] = "-DTRITON_ENABLE_JAVA_HTTP=ON,-DTRITON_ENABLE_JAVA_HTTP=OFF"
PACKAGECONFIG[perf] = "-DTRITON_ENABLE_PERF_ANALYZER=ON,-DTRITON_ENABLE_PERF_ANALYZER=OFF"
PACKAGECONFIG[perf_c] = "-DTRITON_ENABLE_PERF_ANALYZER_C_API=ON,-DTRITON_ENABLE_PERF_ANALYZER_C_API=OFF"
PACKAGECONFIG[perf_tfs] = "-DTRITON_ENABLE_PERF_ANALYZER_TFS=ON,-DTRITON_ENABLE_PERF_ANALYZER_TFS=OFF"
PACKAGECONFIG[perf_ts] = "-DTRITON_ENABLE_PERF_ANALYZER_TS=ON,-DTRITON_ENABLE_PERF_ANALYZER_TS=OFF"
PACKAGECONFIG[gpu] = "-DTRITON_ENABLE_GPU=ON,-DTRITON_ENABLE_GPU=OFF,cuda-cudart"
PACKAGECONFIG[tests] = "-DTRITON_ENABLE_TESTS=ON,-DTRITON_ENABLE_TESTS=OFF,googletest"
PACKAGECONFIG[examples] = "-DTRITON_ENABLE_EXAMPLES=ON,-DTRITON_ENABLE_EXAMPLES=OFF"

do_configure() {
    cmake_do_configure
}

do_compile() {
    cmake_do_compile
    export VERSION="${PV}"
    setuptools3_do_compile
}

do_install:append() {
    rm -f ${D}${prefix}/LICENSE.txt
}

RDEPENDS:${PN} = " \
    python3-six \
    python3-certifi \
"
