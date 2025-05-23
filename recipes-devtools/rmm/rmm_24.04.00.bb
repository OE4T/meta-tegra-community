DESCRIPTION = "RAPIDS Memory Manager"
HOMEPAGE = "https://github.com/rapidsai/rmm"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=86d3f3a95c324c9479bd8986968f4327"

COMPATIBLE_MACHINE = "(cuda)"

inherit cmake pkgconfig cuda

SRC_URI = " \
    git://github.com/rapidsai/rmm.git;protocol=https;nobranch=1;tag=v${PV} \
    file://0001-Updates-for-OE-cross-builds.patch \
"
SRCREV = "8f19c9c3aacf6e612a0cd61f4cf882903bf045aa"

DEPENDS += "fmt spdlog cccl cpm-cmake rapids-cmake"

EXTRA_OECMAKE:append = " \
    -DCPM_SOURCE_CACHE=${RECIPE_SYSROOT}${datadir} \
    -DRAPIDS_CMAKE_DIR=${RECIPE_SYSROOT}/opt/nvidia/rapids-cmake \
    -DBUILD_TESTS=OFF \
"
