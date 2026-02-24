
DESCRIPTION = "The Safe Image Processing Library (SIPL) is NVIDIA’s modular, \
    extensible framework for camera and image sensor integration, image \
    processing, and control, which supports continuous streaming of image data from camera sensors. \
"
LICENSE = "Proprietary"
LIC_FILES_CHKSUM = "file://../../../share/doc/jetson_sipl_api/Tegra_Software_License_Agreement-Tegra-Linux.txt;md5=376d20bd5275442226fcdf54e4844ddf"

SRC_URI = "${L4T_URI_BASE}/Jetson_SIPL_API_R${L4T_VERSION}_aarch64.tbz2"
SRC_URI[sha256sum] = "70adaf47c2d65a0704cacebfca91ab3aa69a0f98f19f5a2dd6e7f20dba8a900c"

inherit l4t_bsp

COMPATIBLE_MACHINE = "(tegra)"

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

ALLOW_EMPTY:${PN} = "1"
PACKAGE_ARCH = "${TEGRA_PKGARCH}"
