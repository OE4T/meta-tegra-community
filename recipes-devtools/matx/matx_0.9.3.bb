DESCRIPTION = "An efficient C++17 GPU numerical computing library"
HOMEPAGE = "https://github.com/NVIDIA/MatX"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=80968939478234f1ff00154a17eda6ed"

inherit cmake cuda

SRC_URI = " \
    git://github.com/NVIDIA/MatX.git;protocol=https;nobranch=1;tag=v${PV} \
    file://0001-Updates-for-OE-cross-builds.patch \
"
SRCREV = "86d0b82d35180480fe8d69729279835ce9b033ff"

DEPENDS += "cccl cpm-cmake rapids-cmake"

EXTRA_OECMAKE:append = " \
    -DCCCL_DIR=${RECIPE_SYSROOT}/opt/nvidia/cccl/lib/cmake/cccl \
    -DCPM_SOURCE_CACHE=${RECIPE_SYSROOT}${datadir} \
    -DRAPIDS_CMAKE_DIR=${RECIPE_SYSROOT}/opt/nvidia/rapids-cmake \
"
