SUMMARY = "A single-header C++ library for simplifying the use of CUDA Runtime Compilation (NVRTC)."
HOMEPAGE = "https://github.com/NVIDIA/jitify"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=469c77aef85c607a7d02a4251443f98f"

SRC_URI = " \
    git://github.com/NVIDIA/jitify.git;protocol=https;branch=master \
    file://0001-Fixups-for-cross-building-in-OE.patch \
"
SRCREV = "1a0ca0e837405506f3b8f7883bacb71c20d86d96"

COMPATIBLE_MACHINE:class-target = "(cuda)"

DEPENDS:append:class-target = " \
    cuda-profiler-api \
    cuda-cudart \
    cuda-nvrtc \
    googletest \
    cccl \
    jitify-native \
"

EXTRA_OEMAKE:class-target = " \
    NVCC="${CUDA_NVCC_EXECUTABLE}" \
    CUDA_DIR="${CUDA_PATH}" \
    CUB_DIR="${RECIPE_SYSROOT}${includedir}/cub" \
    GTEST_LIB_DIR="${RECIPE_SYSROOT}${libdir}" \
    GTEST_INC_DIR="${RECIPE_SYSROOT}${includedir}" \
    STRINGIFY="${STAGING_DIR_NATIVE}${bindir}/stringify" \ 
"

inherit cuda

do_compile:class-native () {
    oe_runmake stringify
}

do_install:class-native () {
    install -d ${D}${bindir}
    install -m 0755 ${B}/stringify ${D}${bindir} 
}

do_install:class-target () {
    install -d ${D}${bindir}
    install -m 0755 ${B}/jitify_example ${D}${bindir}
    install -m 0755 ${B}/jitify_test ${D}${bindir}
    install -m 0755 ${B}/nvrtc_cli ${D}${bindir}
    install -m 0755 ${B}/nvrtc_cli_test.sh ${D}${bindir}/nvrtc_cli_test

    install -d ${D}${includedir}
    install -m 0644 ${B}/jitify.hpp ${D}${includedir}
}

PACKAGES =+ "${PN}-test"

FILES:${PN}-test = "${bindir}/nvrtc_cli_test ${bindir}/jitify_test ${bindir}/jitify_example"
RDEPENDS:${PN}-test += "bash"
INSANE_SKIP:${PN}-test = "ldflags buildpaths"

BBCLASSEXTEND = "native"
