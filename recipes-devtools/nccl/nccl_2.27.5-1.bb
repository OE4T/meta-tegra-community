SUMMARY = "Optimized primitives for collective multi-GPU communication"
HOMEPAGE = "https://github.com/NVIDIA/nccl"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE.txt;md5=a443d82dbb2d29c3bc9ed8be7b9b2e5d"

SRC_URI = " \
    git://github.com/NVIDIA/nccl.git;protocol=https;branch=master;tag=v${PV} \
    file://0001-Fixups-for-cross-building-in-OE.patch \
"
SRCREV = "3ea7eedf3b9b94f1d9f99f4e55536dfcbd23c1ca"

COMPATIBLE_MACHINE = "(cuda)"

inherit cuda python3native

DEPENDS:append = " coreutils-native"

do_compile () {
    export CXX="${CXX_FOR_CUDA}"
    export NVCC="${CUDA_NVCC_EXECUTABLE}"
    export CUDA_HOME="${CUDA_TOOLKIT_ROOT}"
    export NVCC_GENCODE="${CUDA_NVCC_ARCH_FLAGS}"
    export NVCUFLAGS="${CUFLAGS}"
    export NVCUFLAGS_SYM="${CUFLAGS}"
    export CUDA_VERSION="${CUDA_VERSION}"
    oe_runmake src.build
}

do_install () {
    export PREFIX="${D}${prefix}"
    oe_runmake src.install
}

INSANE_SKIP:${PN} = "ldflags"
