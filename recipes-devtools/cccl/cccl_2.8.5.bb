DESCRIPTION = "CUDA Core Compute Libraries"
HOMEPAGE = "https://github.com/NVIDIA/cccl"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"


SRC_URI = "https://github.com/NVIDIA/cccl/releases/download/v${PV}/cccl-v${PV}.tar.gz"
SRC_URI[sha256sum] = "93a4d704fd5179293b392e57cdc98df16cffca613b33fbaded395eb3e35125e4"
UPSTREAM_CHECK_REGEX = "releases/tag/v(?P<pver>\d+(\.\d+)+)"
UPSTREAM_CHECK_URI = "https://github.com/NVIDIA/cccl/releases/"

SRC_URI:append = " file://0001-Updates-for-OE-cross-builds.patch"

inherit cuda

S = "${UNPACKDIR}/${BPN}-v${PV}"
B = "${S}"

do_install() {
    install -d ${D}/opt/nvidia/cccl
    cp -rd --no-preserve=ownership ${B}/include ${D}/opt/nvidia/cccl/
    cp -rd --no-preserve=ownership ${B}/lib ${D}/opt/nvidia/cccl/
}

FILES:${PN} = ""
FILES:${PN}-dev = "/opt/nvidia/cccl"

SYSROOT_DIRS:append = " /opt/nvidia/cccl"

BBCLASSEXTEND = "native nativesdk"
