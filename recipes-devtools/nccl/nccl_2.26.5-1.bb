SUMMARY = "Optimized primitives for collective multi-GPU communication"
HOMEPAGE = "https://github.com/NVIDIA/nccl"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE.txt;md5=a443d82dbb2d29c3bc9ed8be7b9b2e5d"

SRC_URI = " \
    git://github.com/NVIDIA/nccl.git;protocol=https;branch=master;tag=v${PV} \
    file://0001-Fixups-for-cross-building-in-OE.patch \
"
SRCREV = "3000e3c797b4b236221188c07aa09c1f3a0170d4"

COMPATIBLE_MACHINE = "(cuda)"

inherit cuda

DEPENDS += "coreutils-native"

do_compile () {
    export CXX="${CXX_FOR_CUDA}"
    export NVCC="${CUDA_NVCC_EXECUTABLE}"
    export CUDA_HOME="${CUDA_TOOLKIT_ROOT}"
    export NVCC_GENCODE="${CUDA_NVCC_ARCH_FLAGS}"
    export NVCUFLAGS="${CUFLAGS}"
    oe_runmake src.build
}

do_install () {
    export PREFIX="${D}${prefix}"
    oe_runmake src.install
}

INSANE_SKIP:${PN} = "ldflags"
