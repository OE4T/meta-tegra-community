DESCRIPTION = " \
  The NVIDIA Tools Extension SDK (NVTX) is a C-based Application \
  Programming Interface (API) for annotating events, code ranges, and resources \
  in your applications."
HOMEPAGE = "https://github.com/NVIDIA/NVTX"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://include/nvtx3/nvtx3.hpp;endline=15;md5=c03a55fe231fba7bd2f6d33441041844"

SRC_URI = "git://github.com/NVIDIA/NVTX.git;protocol=https;nobranch=1;tag=v${PV}-c-cpp"
SRCREV = "a1ceb0677f67371ed29a2b1c022794f077db5fe7"

inherit cmake

do_install() {
    install -d ${D}/opt/nvidia/nvtx3/include
    cp -R --preserve=mode,links,timestamps ${S}/include/nvtx3/* ${D}/opt/nvidia/nvtx3/include/
}

ALLOW_EMPTY:${PN} = "1"
SYSROOT_DIRS:append = " /opt/nvidia/nvtx3"

FILES:${PN}-dev += "/opt/nvidia/nvtx3"
