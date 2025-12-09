DESCRIPTION = " \
  The NVIDIA Tools Extension SDK (NVTX) is a C-based Application \
  Programming Interface (API) for annotating events, code ranges, and resources \
  in your applications."
HOMEPAGE = "https://github.com/NVIDIA/NVTX"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://include/nvtx3/nvtx3.hpp;endline=19;md5=d530f84c03bf0c0b89cc31e07132384b"

SRC_URI = " \
    git://github.com/NVIDIA/NVTX.git;protocol=https;nobranch=1;tag=v${PV}-c-cpp \
    file://0001-Updates-for-OE-cross-builds.patch \
"
SRCREV = "7c1f2d47f153dac7a0703b162f4b2eaa0ac47c83"

inherit cmake
