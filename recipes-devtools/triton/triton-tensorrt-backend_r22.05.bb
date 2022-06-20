DESCRIPTION = "The Triton backend for TensorRT"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=4e068ed5ea6f9bce3de86a872b056d93"

SRC_URI = " \
    git://github.com/triton-inference-server/tensorrt_backend.git;protocol=https;branch=${PV} \
    file://0001-fix-cmake-build.patch \
"

SRCREV = "94bca597ccc3961cd137567578f9c392cff8c0eb"

S = "${WORKDIR}/git"

DEPENDS = "\
    triton-common \
    triton-core \
    triton-backend \
    tensorrt-core \
    tensorrt-plugins \
    cuda-cudart \
"

inherit cuda cmake

PACKAGECONFIG ??= "gpu"
PACKAGECONFIG[gpu] = "-DTRITON_ENABLE_GPU=ON,-DTRITON_ENABLE_GPU=OFF"
PACKAGECONFIG[stats] = "-DTRITON_ENABLE_STATS=ON,-DTRITON_ENABLE_STATS=OFF"
PACKAGECONFIG[nvtx] = "-DTRITON_ENABLE_NVTX=ON,-DTRITON_ENABLE_NVTX=OFF"

FILES:${PN} += "${libdir}/tensorrt/*"
