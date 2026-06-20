DESCRIPTION = "An efficient C++17 GPU numerical computing library"
HOMEPAGE = "https://github.com/NVIDIA/MatX"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=80968939478234f1ff00154a17eda6ed"

COMPATIBLE_MACHINE = "(cuda)"

inherit cmake cuda

SRC_URI = " \
    git://github.com/NVIDIA/MatX.git;protocol=https;nobranch=1;tag=v${PV} \
    file://0001-Updates-for-OE-cross-builds.patch \
"
SRCREV = "ad55c6b894f0fdef65294858d9561ba31baa8734"

DEPENDS += "cuda-cccl cpm-cmake rapids-cmake"

EXTRA_OECMAKE:append = " \
    -DCCCL_DIR=${RECIPE_SYSROOT}/usr/local/cuda-${CUDA_VERSION}/lib/cmake/cccl \
    -DCPM_SOURCE_CACHE=${RECIPE_SYSROOT}${datadir} \
    -DRAPIDS_CMAKE_DIR=${RECIPE_SYSROOT}/opt/nvidia/rapids-cmake \
    -DFETCHCONTENT_SOURCE_DIR_RAPIDS-CMAKE=${RECIPE_SYSROOT}/opt/nvidia/rapids-cmake \
"
