DESCRIPTION = "Scripts for testing Tegra cuDNN samples"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

SRC_URI = "\
    file://run-cudnn-tests.sh \
"

S = "${UNPACKDIR}"

COMPATIBLE_MACHINE = "(tegra)"

do_install() {
    install -d ${D}${bindir}
    install -m 0755 ${UNPACKDIR}/run-cudnn-tests.sh ${D}${bindir}/run-cudnn-tests
}

PACKAGE_ARCH = "${TEGRA_PKGARCH}"
RDEPENDS:${PN} = "cudnn-samples"
