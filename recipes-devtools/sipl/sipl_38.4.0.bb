
DESCRIPTION = "The Safe Image Processing Library (SIPL) is NVIDIA’s modular, \
    extensible framework for camera and image sensor integration, image \
    processing, and control, which supports continuous streaming of image data from camera sensors. \
"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

L4T_SRCS_NAME = "release"
SRC_URI = "${L4T_URI_BASE}/Jetson_SIPL_API_R${L4T_VERSION}_aarch64.tbz2"
SRC_URI[sha256sum] = "70adaf47c2d65a0704cacebfca91ab3aa69a0f98f19f5a2dd6e7f20dba8a900c"

inherit l4t_bsp

S = "${UNPACKDIR}/usr/src/jetson_sipl_api/sipl"
B = "${S}"

DEPENDS += "tegra-libraries-camera tegra-libraries-nvsci"

do_compile() {
    :
}

do_install() {
    install -d ${D}${includedir}
    install -m 0644 ${S}/fusa/include/*.hpp ${D}${includedir}
    install -m 0644 ${S}/include/nvsci/*.h ${D}${includedir}
}
