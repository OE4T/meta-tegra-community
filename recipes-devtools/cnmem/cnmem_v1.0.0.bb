DESCRIPTION = "A simple memory manager for CUDA designed to help Deep Learning frameworks manage memory"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=927dd052e0c449ac0dd2c0b54d67626d"

SRC_URI = " \
    git://github.com/NVIDIA/cnmem.git;protocol=https;branch=master \
    file://0001-cmake-Set-minimum-required-version-to-3.5-for-CMake-.patch \
"

SRCREV = "c5573333feed2526d4301fca103c55cf3bcb9dcc"

inherit cuda cmake

do_install:append() {
    install -d ${D}${includedir}
    install -m 0644 ${S}/include/cnmem.h ${D}${includedir}
}
