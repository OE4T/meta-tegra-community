DESCRIPTION = "CMake's missing package manager. A small CMake script for \
  setup-free, cross-platform, reproducible dependency management."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=39b0e14f0c3e37b2643eba051401548f"

SRC_URI = " \
    git://github.com/cpm-cmake/CPM.cmake.git;protocol=https;nobranch=1;tag=v${PV} \
    file://0001-update-CPM-version.patch \
"
SRCREV = "d416d9b22ccc9d58bf5b18cf36ab92b58b25a877"

do_configure[noexec] = "1"
do_compile[noexec] = "1"

do_install() {
    install -d ${D}${datadir}/cpm
    install -m 0644 ${S}/cmake/CPM.cmake ${D}${datadir}/cpm/CPM_${PV}.cmake
}

ALLOW_EMPTY:${PN} = "1"
FILES:${PN}-dev += "${datadir}/cpm"
