SUMMARY = "CUDA Python Low-level Bindings"
HOMEPAGE = "https://nvidia.github.io/cuda-python"
LICENSE = "Proprietary"
LIC_FILES_CHKSUM = "file://cuda/LICENSE;md5=06eb7fc760e442991e1d467aa9d5d993"

DEPENDS = "python3-pyclibrary-native python3-cython-native python3-versioneer-native cuda-profiler-api"

SRC_REPO = "github.com/NVIDIA/cuda-python.git;protocol=https"
SRCBRANCH = "main"
SRC_URI = "git://${SRC_REPO};branch=${SRCBRANCH} \
           file://0001-OE-cross-build-fixups.patch \
           "
# v12.6.1 tag
SRCREV = "c4cabc3322c4cce5e1bfa50f28421fb6fdb0030e"

COMPATIBLE_MACHINE = "(tegra)"

S = "${WORKDIR}/git"
PEP517_SOURCE_PATH = "${S}/cuda"

inherit cuda python_setuptools_build_meta

export CUDA_HOME = "${STAGING_DIR_HOST}/usr/local/cuda-${CUDA_VERSION}"
export PARALLEL_LEVEL = "${@oe.utils.cpu_count()}"
PARALLEL_VALUE[vardepvalue] = "1"
CXXFLAGS += "-I${RECIPE_SYSROOT}/usr/local/cuda-${CUDA_VERSION}/include"
CFLAGS += "-I${RECIPE_SYSROOT}/usr/local/cuda-${CUDA_VERSION}/include"
