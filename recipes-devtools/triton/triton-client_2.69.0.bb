DESCRIPTION = "Triton Python, C++ and Java client libraries, and GRPC-generated client examples for go, java and scala."
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=1c4cd58940438cc34ebeb4fec0a79ec8"
SECTION = "libs"

SRC_URI = "\
    git://github.com/triton-inference-server/client.git;protocol=https;branch=r26.05 \
    file://0001-Build-fixups.patch \
"

SRCREV = "18b36fd5af5e9f4f183a8363a0005905560912cd"

DEPENDS = "\
    triton-common \
    triton-core \
"

COMPATIBLE_MACHINE = "(cuda)"

inherit pkgconfig cmake setuptools3 cuda

SETUPTOOLS_SETUP_PATH = "${B}/python-clients/library/generic/wheel"

# setuptools3 replaces the cmake cleandirs; stale ExternalProject stamps
# in ${B} would otherwise skip rebuilding the sub-projects.
do_configure[cleandirs] += "${B}"

EXTRA_OECMAKE += '\
    -DTRITON_VERSION="${PV}" \
'

PACKAGECONFIG ??= "cc_http cc_grpc examples gpu python_http python_grpc"
PACKAGECONFIG[cc_http] = "-DTRITON_ENABLE_CC_HTTP=ON,-DTRITON_ENABLE_CC_HTTP=OFF,re2 curl"
PACKAGECONFIG[cc_grpc] = "-DTRITON_ENABLE_CC_GRPC=ON,-DTRITON_ENABLE_CC_GRPC=OFF,re2 rapidjson grpc protobuf protobuf-native"
PACKAGECONFIG[python_http] = "-DTRITON_ENABLE_PYTHON_HTTP=ON,-DTRITON_ENABLE_PYTHON_HTTP=OFF,,triton-python-backend"
PACKAGECONFIG[python_grpc] = "-DTRITON_ENABLE_PYTHON_GRPC=ON,-DTRITON_ENABLE_PYTHON_GRPC=OFF,python3-grpcio-tools-native,triton-python-backend python3-grpcio python3-protobuf"
PACKAGECONFIG[java] = "-DTRITON_ENABLE_JAVA_HTTP=ON,-DTRITON_ENABLE_JAVA_HTTP=OFF"
PACKAGECONFIG[perf] = "-DTRITON_ENABLE_PERF_ANALYZER=ON,-DTRITON_ENABLE_PERF_ANALYZER=OFF"
PACKAGECONFIG[perf_c] = "-DTRITON_ENABLE_PERF_ANALYZER_C_API=ON,-DTRITON_ENABLE_PERF_ANALYZER_C_API=OFF"
PACKAGECONFIG[perf_tfs] = "-DTRITON_ENABLE_PERF_ANALYZER_TFS=ON,-DTRITON_ENABLE_PERF_ANALYZER_TFS=OFF"
PACKAGECONFIG[perf_ts] = "-DTRITON_ENABLE_PERF_ANALYZER_TS=ON,-DTRITON_ENABLE_PERF_ANALYZER_TS=OFF"
PACKAGECONFIG[gpu] = "-DTRITON_ENABLE_GPU=ON,-DTRITON_ENABLE_GPU=OFF,cuda-cudart"
PACKAGECONFIG[tests] = "-DTRITON_ENABLE_TESTS=ON,-DTRITON_ENABLE_TESTS=OFF,googletest"
PACKAGECONFIG[examples] = "-DTRITON_ENABLE_EXAMPLES=ON,-DTRITON_ENABLE_EXAMPLES=OFF,opencv"

do_configure() {
    cmake_do_configure
}

do_compile() {
    cmake_do_compile
    export VERSION="${PV}"
    setuptools3_do_compile
}

do_install() {
    cmake_do_install
    setuptools3_do_install
    rm -f ${D}${prefix}/LICENSE.txt
}

PACKAGE_BEFORE_PN = "${PN}-examples"

FILES:${PN}-examples = "${bindir}"

SOLIBS = ".so"
FILES_SOLIBSDEV = ""

RDEPENDS:${PN} = " \
    python3-six \
    python3-certifi \
"
