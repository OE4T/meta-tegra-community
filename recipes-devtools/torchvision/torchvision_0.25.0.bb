SUMMARY = "Datasets, Transforms, and Models specific to computer vision"
HOMEPAGE = "https://github.com/pytorch/vision"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=bd7749a3307486a4d4bfefbc81c8b796"

SRC_URI = "git://github.com/pytorch/vision.git;protocol=https;branch=release/0.25;tag=v${PV}"
SRCREV = "8ac84ee75afb1c327902156b5336f56ad63b7e2f"

COMPATIBLE_MACHINE = "(cuda)"

inherit cmake cuda

EXTRA_OECMAKE = " \
    -DWITH_CUDA=1 \
    -DCMAKE_SKIP_RPATH=TRUE \
    -DTORCH_CUDA_ARCH_LIST=${@'.'.join(list(d.getVar('CUDA_ARCHITECTURES')))} \
"

DEPENDS += " \
    jpeg \
    libcublas \
    libpng \
    pytorch \
"

SOLIBS = "*.so*"
FILES_SOLIBSDEV = ""

INSANE_SKIP:${PN} += "buildpaths"
