SUMMARY = "CUDA Python Low-level Bindings"
HOMEPAGE = "https://nvidia.github.io/cuda-python"
LICENSE = "Proprietary"
LIC_FILES_CHKSUM = "file://cuda_bindings/LICENSE;md5=f96b9ded45a4d44d2db90c334b69fed6"

DEPENDS = "python3-pyclibrary-native python3-cython-native python3-setuptools-scm-native cuda-profiler-api"

SRC_REPO = "github.com/NVIDIA/cuda-python.git;protocol=https"
SRCBRANCH = "main"
SRC_URI = "git://${SRC_REPO};branch=${SRCBRANCH} \
           file://0001-OE-cross-build-fixups.patch \
           "
# v13.2.0 tag
SRCREV = "06e60656c0dc2cd110b7603167fded0fff544833"

COMPATIBLE_MACHINE = "(tegra)"

PEP517_SOURCE_PATH = "${S}/cuda_bindings"

inherit cuda python_setuptools_build_meta

# generated bindings embed the parsed CUDA header paths; debug-src package only
INSANE_SKIP:${PN}-src += "buildpaths"

export CUDA_HOME = "${STAGING_DIR_HOST}/usr/local/cuda-${CUDA_VERSION}"
# no git tags after fetch; pin the setuptools_scm version
export SETUPTOOLS_SCM_PRETEND_VERSION = "${PV}"
export PARALLEL_LEVEL = "${@oe.utils.cpu_count()}"
PARALLEL_VALUE[vardepvalue] = "1"
CXXFLAGS += "-I${RECIPE_SYSROOT}/usr/local/cuda-${CUDA_VERSION}/include"
CFLAGS += "-I${RECIPE_SYSROOT}/usr/local/cuda-${CUDA_VERSION}/include"
