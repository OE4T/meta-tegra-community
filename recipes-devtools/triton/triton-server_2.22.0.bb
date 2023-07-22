DESCRIPTION = "The Triton Inference Server provides an optimized cloud and edge inferencing solution."
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=1c4cd58940438cc34ebeb4fec0a79ec8"

SRC_URI = "\
    git://github.com/triton-inference-server/server.git;protocol=https;branch=r22.05 \
    file://0001-Build-fixups.patch \
"

SRCREV = "be579093ac71ce7b55381ff5747a7b9455739cdf"

S = "${WORKDIR}/git"

DEPENDS = "\
    triton-core \
    triton-common \
    triton-backend \
"

COMPATIBLE_MACHINE = "(cuda)"

inherit pkgconfig cmake cuda

DEBUG_PREFIX_MAP:remove:class-target = "-fcanon-prefix-map"

EXTRA_OECMAKE:append = ' \
    -DCMAKE_INSTALL_PREFIX="${D}${prefix}" \
    -DCMAKE_PREFIX_PATH="${STAGING_LIBDIR}/cmake/libevhtp;${STAGING_LIBDIR}/cmake/re2" \
'

PACKAGECONFIG ??= "logging http gpu tensorrt"
PACKAGECONFIG[logging] = "-DTRITON_ENABLE_LOGGING=ON,-DTRITON_ENABLE_LOGGING=OFF"
PACKAGECONFIG[stats] = "-DTRITON_ENABLE_STATS=ON,-DTRITON_ENABLE_STATS=OFF"
PACKAGECONFIG[tracing] = "-DTRITON_ENABLE_TRACING=ON,-DTRITON_ENABLE_TRACING=OFF"
PACKAGECONFIG[nvtx] = "-DTRITON_ENABLE_NVTX=ON,-DTRITON_ENABLE_NVTX=OFF"
PACKAGECONFIG[gpu] = "-DTRITON_ENABLE_GPU=ON,-DTRITON_ENABLE_GPU=OFF"
PACKAGECONFIG[mali_gpu] = "-DTRITON_ENABLE_MALI_GPU=ON,-DTRITON_ENABLE_MALI_GPU=OFF"
PACKAGECONFIG[ensemble] = "-DTRITON_ENABLE_ENSEMBLE=ON,-DTRITON_ENABLE_ENSEMBLE=OFF"
PACKAGECONFIG[http] = "-DTRITON_ENABLE_HTTP=ON,-DTRITON_ENABLE_HTTP=OFF,libevent libevhtp libb64"
PACKAGECONFIG[gprc] = "-DTRITON_ENABLE_GRPC=ON,-DTRITON_ENABLE_GRPC=OFF"
PACKAGECONFIG[sagemaker] = "-DTRITON_ENABLE_SAGEMAKER=ON,-DTRITON_ENABLE_SAGEMAKER=OFF"
PACKAGECONFIG[vertex_ai] = "-DTRITON_ENABLE_VERTEX_AI=ON,-DTRITON_ENABLE_VERTEX_AI=OFF"
PACKAGECONFIG[metrics] = "-DTRITON_ENABLE_METRICS=ON,-DTRITON_ENABLE_METRICS=OFF"
PACKAGECONFIG[metrics_gpu] = "-DTRITON_ENABLE_METRICS_GPU=ON,-DTRITON_ENABLE_METRICS_GPU=OFF"
PACKAGECONFIG[gcs] = "-DTRITON_ENABLE_GCS=ON,-DTRITON_ENABLE_GCS=OFF"
PACKAGECONFIG[s3] = "-DTRITON_ENABLE_S3=ON,-DTRITON_ENABLE_S3=OFF"
PACKAGECONFIG[azure] = "-DTRITON_ENABLE_AZURE_STORAGE=ON,-DTRITON_ENABLE_AZURE_STORAGE=OFF"
PACKAGECONFIG[tensorrt] = "-DTRITON_ENABLE_TENSORRT=ON,-DTRITON_ENABLE_TENSORRT=OFF,,triton-tensorrt-backend"
PACKAGECONFIG[asan] = "-DTRITON_ENABLE_ASAN=ON,-DTRITON_ENABLE_ASAN=OFF"

do_install() {
    install -d ${D}${bindir}
    install -m 0755 ${B}/triton-server/memory_alloc ${D}${bindir}
    install -m 0755 ${B}/triton-server/multi_server ${D}${bindir}
    install -m 0755 ${B}/triton-server/simple ${D}${bindir}
    install -m 0755 ${B}/triton-server/tritonserver ${D}${bindir}
}
