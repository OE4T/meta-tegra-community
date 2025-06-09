DESCRIPTION = "The core library and APIs implementing the Triton Inference Server."
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=5ad619ba25208ce8a327356e0111bb23"
SECTION = "libs"

SRC_URI = "\
    git://github.com/triton-inference-server/core.git;protocol=https;branch=r25.05 \
    file://0001-Build-fixups.patch \
"

SRCREV = "109c69f9715fd95cd6d5fde74e1a6b354a025616"

DEPENDS = "\
    boost \
    cnmem \
    numactl \
    protobuf \
    re2 \
    triton-common \
    python3-pybind11 \
    python3-mypy-native \
"

COMPATIBLE_MACHINE = "(cuda)"

inherit cmake setuptools3 cuda

EXTRA_OECMAKE = '\
    -DTRITON_CORE_HEADERS_ONLY=OFF \
    -DTRITON_VERSION="${PV}" \
'

SETUPTOOLS_SETUP_PATH = "${B}/python"

PACKAGECONFIG ??= "logging gpu"
PACKAGECONFIG[logging] = "-DTRITON_ENABLE_LOGGING=ON,-DTRITON_ENABLE_LOGGING=OFF"
PACKAGECONFIG[stats] = "-DTRITON_ENABLE_STATS=ON,-DTRITON_ENABLE_STATS=OFF"
PACKAGECONFIG[tracing] = "-DTRITON_ENABLE_TRACING=ON,-DTRITON_ENABLE_TRACING=OFF"
PACKAGECONFIG[nvtx] = "-DTRITON_ENABLE_NVTX=ON,-DTRITON_ENABLE_NVTX=OFF"
PACKAGECONFIG[gpu] = "-DTRITON_ENABLE_GPU=ON,-DTRITON_ENABLE_GPU=OFF,cuda-cudart cnmem"
PACKAGECONFIG[mali] = "-DTRITON_ENABLE_MALI_GPU=ON,-DTRITON_ENABLE_MALI_GPU=OFF"
PACKAGECONFIG[ensemble] = "-DTRITON_ENABLE_ENSEMBLE=ON,-DTRITON_ENABLE_ENSEMBLE=OFF"
PACKAGECONFIG[metrics] = "-DTRITON_ENABLE_METRICS=ON,-DTRITON_ENABLE_METRICS=OFF"
PACKAGECONFIG[metrics_cpu] = "-DTRITON_ENABLE_METRICS_CPU=ON,-DTRITON_ENABLE_METRICS_CPU=OFF"
PACKAGECONFIG[metrics_gpu] = "-DTRITON_ENABLE_METRICS_GPU=ON,-DTRITON_ENABLE_METRICS_GPU=OFF"
PACKAGECONFIG[gcs] = "-DTRITON_ENABLE_GCS=ON,-DTRITON_ENABLE_GCS=OFF"
PACKAGECONFIG[s3] = "-DTRITON_ENABLE_S3=ON,-DTRITON_ENABLE_S3=OFF"
PACKAGECONFIG[azure] = "-DTRITON_ENABLE_AZURE_STORAGE=ON,-DTRITON_ENABLE_AZURE_STORAGE=OFF"

do_configure() {
    cmake_do_configure
}

do_compile() {
    cmake_do_compile
    export VERSION="${PV}"
    export TRITON_PYBIND="${B}/python/tritonserver"
    export STUBGEN_CLI="${STAGING_BINDIR_NATIVE}/stubgen"
    setuptools3_do_compile
}

do_install:append() {
    install -d ${D}${libdir}
    install -m 0644 ${B}/triton-core/libtriton-core.so ${D}${libdir}
    install -m 0644 ${B}/libtriton-core-serverstub.so ${D}${libdir}

    install -d ${D}${includedir}/triton/core
    install -m 0644 ${S}/include/triton/core/tritonbackend.h ${D}${includedir}/triton/core/
    install -m 0644 ${S}/include/triton/core/tritoncache.h ${D}${includedir}/triton/core/
    install -m 0644 ${S}/include/triton/core/tritonrepoagent.h ${D}${includedir}/triton/core/
    install -m 0644 ${S}/include/triton/core/tritonserver.h ${D}${includedir}/triton/core/

    rm -rf ${D}${libdir}/stubs
    rm -f ${D}${prefix}/LICENSE.txt
}

SOLIBS = ".so"
FILES_SOLIBSDEV = ""

FILES:${PN}-dev += ""

INSANE_SKIP:${PN} += "already-stripped"
