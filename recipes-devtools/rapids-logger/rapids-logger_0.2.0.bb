DESCRIPTION = "A logging interface for RAPIDS built on spdlog"
HOMEPAGE = "https://github.com/rapidsai/rapids-logger"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=86d3f3a95c324c9479bd8986968f4327"

SRC_URI = " \
    git://github.com/rapidsai/rapids-logger.git;protocol=https;nobranch=1 \
    file://0001-Updates-for-OE-cross-builds.patch \
"
SRCREV = "9db6afe15ea80766bbfb65747a9189b1d671df5b"

inherit cmake pkgconfig

DEPENDS += "fmt spdlog cpm-cmake rapids-cmake"

EXTRA_OECMAKE:append = " \
    -DCPM_SOURCE_CACHE=${RECIPE_SYSROOT}${datadir} \
    -DRAPIDS_CMAKE_DIR=${RECIPE_SYSROOT}/opt/nvidia/rapids-cmake \
    -DBUILD_TESTS=OFF \
"

SOLIBS = "*.so*"
FILES_SOLIBSDEV = ""
