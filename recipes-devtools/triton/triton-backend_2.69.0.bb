DESCRIPTION = "Common source, scripts and utilities for creating Triton backends."
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=124771e398c4a908eba9a40eaa7903e5"
SECTION = "libs"

SRC_URI = "\
    git://github.com/triton-inference-server/backend.git;protocol=https;branch=r26.05 \
    file://0001-Build-fixups.patch \
"

SRCREV = "3f57fb90bee16b86232ee79c45eb4ce69c9ad489"

DEPENDS = " \
    triton-common \
    triton-core \
"

COMPATIBLE_MACHINE = "(cuda)"

inherit cmake cuda


PACKAGECONFIG ??= "gpu stats"
PACKAGECONFIG[gpu] = "-DTRITON_ENABLE_GPU=ON,-DTRITON_ENABLE_GPU=OFF"
PACKAGECONFIG[mali_gpu] = "-DTRITON_ENABLE_MALI_GPU=ON,-DTRITON_ENABLE_MALI_GPU=OFF"
PACKAGECONFIG[stats] = "-DTRITON_ENABLE_STATS=ON,-DTRITON_ENABLE_STATS=OFF"
