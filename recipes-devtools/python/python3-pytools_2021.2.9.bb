SUMMARY = "Python tools"
HOMEPAGE = "https://documen.tician.de/pytools/"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=43088e5b779c4ff00705b42785d32e23"

SRC_URI[sha256sum] = "db6cf83c9ba0a165d545029e2301621486d1e9ef295684072e5cd75316a13755"

S = "${WORKDIR}/pytools-${PV}"

inherit pypi setuptools3

RDEPENDS_${PN} += "\
    python3-decorator \
    python3-appdirs \
    python3-six \
    python3-numpy \
"
