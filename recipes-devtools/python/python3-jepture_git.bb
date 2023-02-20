SUMMARY = "A simple python library to quickly capture images on the Jetson."
LICENSE = "Proprietary & BSD-3-Clause & MIT"
LIC_FILES_CHKSUM = " \
    file://LICENSE.md;md5=4f0a72805ccc91536540a1cf89a935ec \
    file://jetson_multimedia_api/usr/src/jetson_multimedia_api/argus/LICENSE.TXT;md5=271791ce6ff6f928d44a848145021687 \
"

COMPATIBLE_MACHINE = "(tegra)"

SRC_COMMON_DEBS = "${@l4t_deb_pkgname(d, 'jetson-multimedia-api')};subdir=git/jetson_multimedia_api"

inherit setuptools3 l4t_deb_pkgfeed

SRC_URI =+ " \
    git://github.com/DelSkayn/jepture.git;protocol=https;branch=main \
    file://0001-setup.py-don-t-hardcode-paths.patch \
    file://0002-v4l2-remove-duplicate-symbols-from-nvidia-extensions.patch \
"

SRC_URI[sha256sum] = "a436cbfdbbef87b9be8e0defb4c6af5ffa4c4ba8bd957446577a9fb30334595f"
SRCREV = "be40d9219e0f2eb69141f632181ae8c0ba969413"
PV = "git${SRCPV}"

S = "${WORKDIR}/git"

DEPENDS = " \
    python3-pybind11-native \
    tegra-libraries-multimedia \
    tegra-libraries-camera \
"
