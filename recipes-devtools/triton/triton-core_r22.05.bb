DESCRIPTION = "The core library and APIs implementing the Triton Inference Server."
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=5ad619ba25208ce8a327356e0111bb23"
SECTION = "libs"

SRC_URI = "\
    git://github.com/triton-inference-server/core.git;protocol=https;branch=${PV} \
    file://0001-fix-cmake-build.patch \
"

SRCREV = "70506c503ac44958d8531a6f51f4f3be8241c362"

S = "${WORKDIR}/git"

DEPENDS = "\
    boost \
    cnmem \
    numactl \
    protobuf \
    re2 \
    triton-common \
"

inherit cmake cuda

EXTRA_OECMAKE = "\
    -DTRITON_CORE_HEADERS_ONLY=OFF \
    -DCMAKE_INSTALL_PREFIX=/usr \
    -DCMAKE_CXX_FLAGS='${CMAKE_CXX_FLAGS} -Wno-maybe-uninitialized' \
"

PACKAGECONFIG ??= "logging gpu"
PACKAGECONFIG[logging] = "-DTRITON_ENABLE_LOGGING=ON,-DTRITON_ENABLE_LOGGING=OFF"
PACKAGECONFIG[stats] = "-DTRITON_ENABLE_STATS=ON,-DTRITON_ENABLE_STATS=OFF"
PACKAGECONFIG[tracing] = "-DTRITON_ENABLE_TRACING=ON,-DTRITON_ENABLE_TRACING=OFF"
PACKAGECONFIG[nvtx] = "-DTRITON_ENABLE_NVTX=ON,-DTRITON_ENABLE_NVTX=OFF"
PACKAGECONFIG[gpu] = "-DTRITON_ENABLE_GPU=ON,-DTRITON_ENABLE_GPU=OFF,cuda-cudart cnmem"
PACKAGECONFIG[mali] = "-DTRITON_ENABLE_MALI_GPU=ON,-DTRITON_ENABLE_MALI_GPU=OFF"
PACKAGECONFIG[ensemble] = "-DTRITON_ENABLE_ENSEMBLE=ON,-DTRITON_ENABLE_ENSEMBLE=OFF"
PACKAGECONFIG[metrics] = "-DTRITON_ENABLE_METRICS=ON,-DTRITON_ENABLE_METRICS=OFF"
PACKAGECONFIG[metrics_gpu] = "-DTRITON_ENABLE_METRICS_GPU=ON,-DTRITON_ENABLE_METRICS_GPU=OFF"
PACKAGECONFIG[gcs] = "-DTRITON_ENABLE_GCS=ON,-DTRITON_ENABLE_GCS=OFF"
PACKAGECONFIG[s3] = "-DTRITON_ENABLE_S3=ON,-DTRITON_ENABLE_S3=OFF"
PACKAGECONFIG[azure] = "-DTRITON_ENABLE_AZURE_STORAGE=ON,-DTRITON_ENABLE_AZURE_STORAGE=OFF"

do_install:append() {
    DESTDIR='${D}' eval ${DESTDIR:+DESTDIR=${DESTDIR} }${CMAKE_VERBOSE} cmake --build '${B}/triton-core' "$@"  --target ${OECMAKE_TARGET_INSTALL} -- ${EXTRA_OECMAKE_BUILD} 
    rm -rf ${D}${libdir}/stubs
    mv ${D}${libdir}/libtriton-core.so ${D}${libdir}/libtriton-core.so.${PV}
    ln -sr ${D}${libdir}/libtriton-core.so.${PV} ${D}${libdir}/libtriton-core.so
}
