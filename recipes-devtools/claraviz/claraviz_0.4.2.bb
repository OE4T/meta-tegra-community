DESCRIPTION = "NVIDIA Clara Viz is a platform for visualization of 2D/3D medical imaging data"
HOMEPAGE = "https://github.com/NVIDIA/clara-viz"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=7bb1df9531208a3d5eaeaa657cc1205b"

SRC_URI = "git://github.com/NVIDIA/clara-viz.git;protocol=https;nobranch=1;tag=v${PV}"
SRCREV = "392af55b120a68f5114f886f3f7302dd4ebb5338"

SRC_URI += " \
    file://0001-Updates-for-OE-cross-builds.patch \
"

COMPATIBLE_MACHINE = "(cuda)"

inherit pkgconfig cmake cuda

EXTRA_OECMAKE:append = " \
    -DCLARA_VIZ_WITH_GRPC=OFF \
    -DCLARA_VIZ_WITH_OPENH264=OFF \
    -DCLARA_VIZ_WITH_EXAMPLES=OFF \
"

CUDA_NVCC_EXTRA_FLAGS += " \
    --use_fast_math \
    --expt-relaxed-constexpr \
"

do_install:append() {
    install -m 0644 ${S}/cmake/clara_viz_renderer-targets.cmake ${D}${libdir}/cmake/claraviz
    install -m 0644 ${S}/lib/${TARGET_ARCH}/libclara_viz_renderer.so.${PV} ${D}${libdir}
    ln -s libclara_viz_renderer.so.${PV} ${D}${libdir}/libclara_viz_renderer.so.0
    ln -s libclara_viz_renderer.so.0 ${D}${libdir}/libclara_viz_renderer.so
}

DEPENDS += " \
    libnvjpeg \
    nlohmann-json \
    zlib \
"

INSANE_SKIP:${PN} += "already-stripped"
INSANE_SKIP:${PN}-dev += "buildpaths"
INSANE_SKIP:${PN}-staticdev += "buildpaths"
