SUMMARY = "Unified Communication X"
HOMEPAGE = "https://github.com/openucx/ucx"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=cbe4fe88c540f18985ee4d32d590f683"

SRC_URI = " \
    git://github.com/openucx/ucx.git;protocol=https;nobranch=1;tag=v${PV} \
    file://0001-fix-cmake-library-import-paths.patch \
    file://0002-add-option-to-enable-NVML.patch \
    file://0003-add-inclusion-of-omp.h-outside-the-extern-C-block.patch \
"
SRCREV = "e4636149592d5a435c2c911fe7727444a13bfa2e"

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

EXTRA_OEMAKE += "NVCC='${CUDA_NVCC_EXECUTABLE} -ccbin ${CUDAHOSTCXX} ${CUDAFLAGS}'"

FILES:${PN}-dev += "\
    ${libdir}/*.so \
    ${libdir}/ucx/*.so \
"

INSANE_SKIP:${PN} = "buildpaths"
