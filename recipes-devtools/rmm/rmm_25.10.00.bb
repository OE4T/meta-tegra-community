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
SRCREV = "7aaad1dee0690a48db8c92210593f6c70f6f7648"

DEPENDS += "cuda-cccl nvtx cpm-cmake rapids-cmake rapids-logger"

OECMAKE_SOURCEPATH = "${S}/cpp"

EXTRA_OECMAKE:append = " \
    -DCCCL_DIR=${RECIPE_SYSROOT}/usr/local/cuda-${CUDA_VERSION}/lib/cmake/cccl \
    -DCPM_SOURCE_CACHE=${RECIPE_SYSROOT}${datadir} \
    -DRAPIDS_CMAKE_DIR=${RECIPE_SYSROOT}/opt/nvidia/rapids-cmake \
    -DRMM_NVTX=ON \
    -DBUILD_TESTS=OFF \
"

SOLIBS = "*.so*"
FILES_SOLIBSDEV = ""
