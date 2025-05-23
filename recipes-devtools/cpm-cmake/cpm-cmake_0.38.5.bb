DESCRIPTION = "CMake's missing package manager. A small CMake script for \
  setup-free, cross-platform, reproducible dependency management."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=39b0e14f0c3e37b2643eba051401548f"

SRC_URI = "git://github.com/cpm-cmake/CPM.cmake.git;protocol=https;nobranch=1;tag=v${PV}"
SRCREV = "d6d5d0d5abca0b9ffe253353f75befc704e81bec"

do_configure[noexec] = "1"
do_compile[noexec] = "1"

do_install() {
    install -d ${D}${datadir}/cpm
    install -m 0644 ${S}/cmake/CPM.cmake ${D}${datadir}/cpm/CPM_${PV}.cmake
}

ALLOW_EMPTY:${PN} = "1"
FILES:${PN}-dev += "${datadir}/cpm"
