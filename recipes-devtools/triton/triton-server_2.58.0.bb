DESCRIPTION = "The Triton Inference Server provides an optimized cloud and edge inferencing solution."
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=cbffd7f72014cdf056bc4e6f176b0c6b"

SRC_URI = "\
    git://github.com/triton-inference-server/server.git;protocol=https;branch=r25.05;lfs=0 \
    file://0001-Build-fixups.patch \
"

SRCREV = "af417a8ae7457c17ebb44ecda204367f00d11ced"

DEPENDS += "\
    triton-core \
    triton-common \
    triton-backend \
    re2 \
    python3-pybind11 \
"

COMPATIBLE_MACHINE = "(cuda)"

inherit pkgconfig cmake setuptools3 cuda

B = "${S}"

CXXFLAGS += "${CUDA_CXXFLAGS}"

EXTRA_OECMAKE:append = ' \
    -DCMAKE_INSTALL_PREFIX="${D}${prefix}" \
    -DCMAKE_PREFIX_PATH=${STAGING_DIR_TARGET}/usr \
    -DTRITON_VERSION="${PV}" \
'

SETUPTOOLS_SETUP_PATH = "${B}/src/python"

PACKAGECONFIG ??= "logging http gpu tensorrt"
PACKAGECONFIG[logging] = "-DTRITON_ENABLE_LOGGING=ON,-DTRITON_ENABLE_LOGGING=OFF"
PACKAGECONFIG[stats] = "-DTRITON_ENABLE_STATS=ON,-DTRITON_ENABLE_STATS=OFF"
PACKAGECONFIG[tracing] = "-DTRITON_ENABLE_TRACING=ON,-DTRITON_ENABLE_TRACING=OFF"
PACKAGECONFIG[nvtx] = "-DTRITON_ENABLE_NVTX=ON,-DTRITON_ENABLE_NVTX=OFF"
PACKAGECONFIG[gpu] = "-DTRITON_ENABLE_GPU=ON,-DTRITON_ENABLE_GPU=OFF"
PACKAGECONFIG[mali_gpu] = "-DTRITON_ENABLE_MALI_GPU=ON,-DTRITON_ENABLE_MALI_GPU=OFF"
PACKAGECONFIG[ensemble] = "-DTRITON_ENABLE_ENSEMBLE=ON,-DTRITON_ENABLE_ENSEMBLE=OFF"
PACKAGECONFIG[http] = "-DTRITON_ENABLE_HTTP=ON,-DTRITON_ENABLE_HTTP=OFF,libevent libevhtp libb64"
PACKAGECONFIG[grpc] = "-DTRITON_ENABLE_GRPC=ON,-DTRITON_ENABLE_GRPC=OFF"
PACKAGECONFIG[sagemaker] = "-DTRITON_ENABLE_SAGEMAKER=ON,-DTRITON_ENABLE_SAGEMAKER=OFF"
PACKAGECONFIG[vertex_ai] = "-DTRITON_ENABLE_VERTEX_AI=ON,-DTRITON_ENABLE_VERTEX_AI=OFF"
PACKAGECONFIG[metrics] = "-DTRITON_ENABLE_METRICS=ON,-DTRITON_ENABLE_METRICS=OFF"
PACKAGECONFIG[metrics_cpu] = "-DTRITON_ENABLE_METRICS_CPU=ON,-DTRITON_ENABLE_METRICS_CPU=OFF"
PACKAGECONFIG[metrics_gpu] = "-DTRITON_ENABLE_METRICS_GPU=ON,-DTRITON_ENABLE_METRICS_GPU=OFF"
PACKAGECONFIG[gcs] = "-DTRITON_ENABLE_GCS=ON,-DTRITON_ENABLE_GCS=OFF"
PACKAGECONFIG[s3] = "-DTRITON_ENABLE_S3=ON,-DTRITON_ENABLE_S3=OFF"
PACKAGECONFIG[azure] = "-DTRITON_ENABLE_AZURE_STORAGE=ON,-DTRITON_ENABLE_AZURE_STORAGE=OFF"
PACKAGECONFIG[tensorrt] = "-DTRITON_ENABLE_TENSORRT=ON,-DTRITON_ENABLE_TENSORRT=OFF,,triton-tensorrt-backend"
PACKAGECONFIG[asan] = "-DTRITON_ENABLE_ASAN=ON,-DTRITON_ENABLE_ASAN=OFF"

do_configure() {
    cmake_do_configure
}

do_compile() {
    cmake_do_compile
    install -m 0644 ${B}/LICENSE ${B}/src/python/LICENSE.txt
    export VERSION="${PV}"
    export TRITON_PYBIND="${B}/src/python/tritonfrontend"
    setuptools3_do_compile
}

do_install:append() {
    install -d ${D}${bindir}
    install -m 0755 ${B}/triton-server/memory_alloc ${D}${bindir}
    install -m 0755 ${B}/triton-server/multi_server ${D}${bindir}
    install -m 0755 ${B}/triton-server/simple ${D}${bindir}
    install -m 0755 ${B}/triton-server/tritonserver ${D}${bindir}
    install -d ${D}${libdir}
    install -m 0644 ${B}/triton-server/libhttp-endpoint-library.a ${D}${libdir}
    rm -f ${D}${prefix}/LICENSE.txt
}
