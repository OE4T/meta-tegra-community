DESCRIPTION = "RAPIDS - UCXX"
HOMEPAGE = "https://github.com/rapidsai/ucxx"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=4ec284a3e8cabd4193beb46eb54ba7af"

COMPATIBLE_MACHINE = "(cuda)"

inherit cmake pkgconfig cuda

SRC_URI = " \
    git://github.com/rapidsai/ucxx.git;protocol=https;nobranch=1;tag=v${PV} \
    file://0001-Updates-for-OE-cross-builds.patch \
"
SRCREV = "7b160b312b15cecbed9932145c53e760d7a1dd62"

DEPENDS += "ucx cuda-cccl cpm-cmake rapids-cmake"

OECMAKE_SOURCEPATH = "${S}/cpp"

EXTRA_OECMAKE:append = " \
    -DCCCL_DIR=${RECIPE_SYSROOT}/usr/local/cuda-${CUDA_VERSION}/lib/cmake/cccl \
    -DCPM_SOURCE_CACHE=${RECIPE_SYSROOT}${datadir} \
    -DRAPIDS_CMAKE_DIR=${RECIPE_SYSROOT}/opt/nvidia/rapids-cmake \
    -DBUILD_TESTS=OFF \
"

SOLIBS = ".so"
FILES_SOLIBSDEV = ""
