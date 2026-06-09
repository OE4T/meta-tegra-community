SUMMARY = "Datasets, Transforms, and Models specific to computer vision"
HOMEPAGE = "https://github.com/pytorch/vision"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=bd7749a3307486a4d4bfefbc81c8b796"

SRC_URI = "git://github.com/pytorch/vision.git;protocol=https;branch=release/0.26;tag=v${PV}"
SRCREV = "336d36e8db990a905498c73933e35231876e28bc"

COMPATIBLE_MACHINE = "(cuda)"

inherit cmake cuda

EXTRA_OECMAKE = " \
    -DWITH_CUDA=1 \
    -DCMAKE_SKIP_RPATH=TRUE \
    -DTORCH_CUDA_ARCH_LIST=${@' '.join(['%s.%s' % (a[:-1], a[-1]) for a in d.getVar('CUDA_ARCHITECTURES').split()])} \
    -DCMAKE_CUDA_IMPLICIT_INCLUDE_DIRECTORIES=${RECIPE_SYSROOT}/usr/include \
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
