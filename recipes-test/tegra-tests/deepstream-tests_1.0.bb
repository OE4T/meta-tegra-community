DESCRIPTION = "Scripts for testing Tegra DeepStream 6.3 samples"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

SRC_URI = "\
    file://run-deepstream-tests.sh \
"

S = "${UNPACKDIR}"

COMPATIBLE_MACHINE = "(tegra)"

do_install() {
    install -d ${D}${bindir}
    install -m 0755 ${UNPACKDIR}/run-deepstream-tests.sh ${D}${bindir}/run-deepstream-tests
}

PACKAGE_ARCH = "${TEGRA_PKGARCH}"
RDEPENDS:${PN} = " \
    deepstream-7.1 \
    deepstream-7.1-samples \
    deepstream-7.1-pyds \
    deepstream-7.1-pyds-samples \
    tegra-tools-jetson-clocks \
"
