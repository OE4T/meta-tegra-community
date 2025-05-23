DESCRIPTION = "CUDA Core Compute Libraries"
HOMEPAGE = "https://github.com/NVIDIA/cccl"
LICENSE = "Apache-2.0 & BSD-3-Clause & MIT & Proprietary & BSL-1.0 & BSD-2-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=d77238474f6020834e49307d63d16ec5"

COMPATIBLE_MACHINE = "(cuda)"

inherit cmake cuda

SRC_URI = " \
    git://github.com/NVIDIA/cccl.git;protocol=https;nobranch=1;tag=v${PV} \
    file://0001-Updates-for-OE-cross-builds.patch \
"
SRCREV = "36f379f29660761fe033a1306ca9dab6a88cb65c"

B = "${S}"

EXTRA_OECMAKE:append = " \
    -DCCCL_ENABLE_TESTING=OFF \
    -DCUB_SOURCE_DIR=${B}/cub \
    -DCUB_ENABLE_TESTING=OFF \
    -DCUB_ENABLE_EXAMPLES=OFF \
    -DTHRUST_ENABLE_HEADER_TESTING=OFF \
    -DTHRUST_ENABLE_TESTING=OFF \
    -DTHRUST_ENABLE_EXAMPLES=OFF \
    -DLIBCUDACXX_ENABLE_LIBCUDACXX_TESTS=OFF \
"

CUDA_NVCC_EXTRA_FLAGS:append = "-I${S}/cub -I${S}/libcudacxx -I${S}/thrust"
