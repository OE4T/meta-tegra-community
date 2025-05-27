SUMMARY = "Unified Communication X"
HOMEPAGE = "https://github.com/openucx/ucx"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=cbe4fe88c540f18985ee4d32d590f683"

SRC_URI = " \
    git://github.com/openucx/ucx.git;protocol=https;branch=v1.18.x;tag=v${PV} \
    file://0001-Fix-CMAKE-library-import-paths.patch \
    file://0002-Add-option-to-enable-NVML.patch \
    file://0003-fix-build-issues-with-gcc-15.patch \
    file://0004-add-inclusion-of-omp.h-outside-the-extern-C-block.patch \
"
SRCREV = "d9aa5650d4cbcbb00d61af980614dbe9dd27a1f2"

COMPATIBLE_MACHINE = "(cuda)"

inherit autotools pkgconfig cuda

PACKAGECONFIG ??= "cuda nvml"

PACKAGECONFIG[cuda] = "--with-cuda=${RECIPE_SYSROOT}/usr/local/cuda-${CUDA_VERSION},--without-cuda"
PACKAGECONFIG[nvml] = "--enable-nvml,,cuda-nvml"
PACKAGECONFIG[verbs] = "--with-verbs=${RECIPE_SYSROOT}${prefix},--without-verbs, librdmacm1 libnl libibverbs1"
PACKAGECONFIG[rdmacm] = "--with-rdmacm=${RECIPE_SYSROOT}${prefix},--without-rdmacm, librdmacm1 libnl libibverbs1"
PACKAGECONFIG[mlx5] = "--with-mlx5-dv,"

EXTRA_OECONF:append = " \
    --disable-logging \
    --disable-debug \
    --disable-assertions \
    --disable-params-check \
    --enable-mt \
"

INSANE_SKIP:${PN} += "dev-so buildpaths"
