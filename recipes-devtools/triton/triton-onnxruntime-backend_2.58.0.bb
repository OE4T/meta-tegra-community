DESCRIPTION = "The Triton backend for ONNX Runtime."
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=7889848dc86811b06ded7bfa9ba39e02"

SRC_URI = " \
    git://github.com/triton-inference-server/onnxruntime_backend.git;protocol=https;branch=r25.05 \
    file://0001-Build-fixups.patch \
"

SRCREV = "cf4cd89bf66ed1d7fb89c2b0930d43bcce3c5f97"

DEPENDS = "\
    triton-common \
    triton-core \
    triton-backend \
    onnxruntime \
    cuda-cudart \
"

COMPATIBLE_MACHINE = "(cuda)"

inherit pkgconfig cuda cmake

EXTRA_OECMAKE:append = " \
    -DTRITON_ONNXRUNTIME_LIB_PATHS=${STAGING_LIBDIR} \
"

PACKAGECONFIG ??= "gpu"
PACKAGECONFIG[gpu] = "-DTRITON_ENABLE_GPU=ON,-DTRITON_ENABLE_GPU=OFF"
PACKAGECONFIG[stats] = "-DTRITON_ENABLE_STATS=ON,-DTRITON_ENABLE_STATS=OFF"

FILES:${PN} += "${libdir}/onnxruntime/*"
