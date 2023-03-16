SUMMARY = "Python tools"
HOMEPAGE = "https://documen.tician.de/pytools/"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=43088e5b779c4ff00705b42785d32e23"

SRC_URI[sha256sum] = "41017371610bb2a03685597c5285205e6597c7f177383d95c8b871244b12c14e"

S = "${WORKDIR}/pytools-${PV}"

inherit pypi setuptools3

RDEPENDS:${PN} += "\
    python3-decorator \
    python3-appdirs \
    python3-six \
    python3-numpy \
"
