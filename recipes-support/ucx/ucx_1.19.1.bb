SUMMARY = "Unified Communication X"
HOMEPAGE = "https://github.com/openucx/ucx"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=cbe4fe88c540f18985ee4d32d590f683"

SRC_URI = " \
    git://github.com/openucx/ucx.git;protocol=https;nobranch=1;tag=v${PV} \
    file://0001-fix-cmake-library-import-paths.patch \
    file://0002-add-option-to-enable-nvml.patch \
    file://0003-Remove-cross-compile-CUDA-test.patch \
"
SRCREV = "7009d7a19b1c2464224a3fe117a4155fb29298f5"

COMPATIBLE_MACHINE = "(cuda)"

inherit autotools pkgconfig cuda

PACKAGECONFIG ??= "cuda nvml"

PACKAGECONFIG[cuda] = "--with-cuda=${RECIPE_SYSROOT}/usr/local/cuda-${CUDA_VERSION},--without-cuda"
PACKAGECONFIG[nvml] = "--enable-nvml,,cuda-nvml"
PACKAGECONFIG[verbs] = "--with-verbs=${RECIPE_SYSROOT}${prefix},--without-verbs, librdmacm1 libnl libibverbs1"
PACKAGECONFIG[rdmacm] = "--with-rdmacm=${RECIPE_SYSROOT}${prefix},--without-rdmacm, librdmacm1 libnl libibverbs1"
PACKAGECONFIG[mlx5] = "--with-mlx5,--without-mlx5, ibverbs-providers"

EXTRA_OECONF:append = " \
    --disable-logging \
    --disable-debug \
    --disable-assertions \
    --disable-params-check \
    --enable-mt \
    --without-go \
    --without-java \
"

INSANE_SKIP:${PN} = "dev-so buildpaths"
