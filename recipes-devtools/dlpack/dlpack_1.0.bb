DESCRIPTION = "DLPack: Open In Memory Tensor Structure"
HOMEPAGE = "https://github.com/dmlc/dlpack"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=f62d4e85ba68a1574b74d97ab8dea9ab"

SRC_URI = " \
    git://github.com/dmlc/dlpack.git;protocol=https;branch=main;tag=v${PV} \
    file://0001-update-dlpack-project-version.patch \
    file://0002-cmake-Set-minimum-required-version-to-3.5-for-CMake-.patch \
"
SRCREV = "bbd2f4d32427e548797929af08cfe2a9cbb3cf12"

inherit cmake

ALLOW_EMPTY:${PN} = "1"
