SUMMARY = "A simple python library to quickly capture images on the Jetson."
LICENSE = "Proprietary & BSD-3-Clause & MIT"
LIC_FILES_CHKSUM = " \
    file://LICENSE.md;md5=4f0a72805ccc91536540a1cf89a935ec \
    file://jetson_multimedia_api/usr/src/jetson_multimedia_api/argus/LICENSE.TXT;md5=271791ce6ff6f928d44a848145021687 \
"

COMPATIBLE_MACHINE = "(tegra)"

L4T_DEB_SOCNAME = "t194"
PKG_VER = "${L4T_VERSION}${@l4t_bsp_debian_version_suffix(d, pkgname='nvidia-l4t-jetson-multimedia-api')}"
SRC_COMMON_DEBS = "nvidia-l4t-jetson-multimedia-api_${PKG_VER}_arm64.deb;subdir=git/jetson_multimedia_api"

inherit setuptools3 l4t_deb_pkgfeed

SRC_URI =+ " \
    git://github.com/DelSkayn/jepture.git;protocol=https;branch=main \
    file://0001-setup.py-don-t-hardcode-paths.patch \
    file://0002-v4l2-remove-duplicate-symbols-from-nvidia-extensions.patch \
    file://0003-Revert-argus_stream-update-certain-symbols-to-work-w.patch \
"

SRC_URI[sha256sum] = "7cf134ea26da03c12421ff91c2f9a0051d655704bfbb6917ff1d5ed678bd6600"
SRCREV = "be40d9219e0f2eb69141f632181ae8c0ba969413"
PV = "git${SRCPV}"

S = "${WORKDIR}/git"

DEPENDS = " \
    python3-pybind11-native \
    tegra-libraries-multimedia \
    tegra-libraries-camera \
"
