DESCRIPTION = "Scripts for testing Tegra TensorRT samples"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

SRC_URI = "\
    file://run-tensorrt-tests.sh \
"

S = "${UNPACKDIR}"

COMPATIBLE_MACHINE = "(tegra)"

do_install() {
    install -d ${D}${bindir}
    install -m 0755 ${UNPACKDIR}/run-tensorrt-tests.sh ${D}${bindir}/run-tensorrt-tests
}

PACKAGE_ARCH = "${TEGRA_PKGARCH}"
RDEPENDS:${PN} = "tensorrt-samples tensorrt-trtexec tegra-tools-jetson-clocks wget bash"
