DESCRIPTION = "An efficient C++17 GPU numerical computing library"
HOMEPAGE = "https://github.com/NVIDIA/MatX"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=80968939478234f1ff00154a17eda6ed"

inherit cmake cuda

SRC_URI = " \
    git://github.com/NVIDIA/MatX.git;protocol=https;nobranch=1;tag=v${PV} \
    file://0001-Updates-for-OE-cross-builds.patch \
"
SRCREV = "fa9e8728bf61c12ca140df93a87322c48886d1aa"

DEPENDS += "cuda-cccl cpm-cmake rapids-cmake"

EXTRA_OECMAKE:append = " \
    -DCCCL_DIR=${RECIPE_SYSROOT}/usr/local/cuda-${CUDA_VERSION}/lib/cmake/cccl \
    -DCPM_SOURCE_CACHE=${RECIPE_SYSROOT}${datadir} \
    -DRAPIDS_CMAKE_DIR=${RECIPE_SYSROOT}/opt/nvidia/rapids-cmake \
"
