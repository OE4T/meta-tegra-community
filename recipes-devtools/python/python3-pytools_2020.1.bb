SUMMARY = "Python tools"
HOMEPAGE = "https://documen.tician.de/pytools/"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=43088e5b779c4ff00705b42785d32e23"

SRC_URI[md5sum] = "7cfa4fdce5b54184e62b14eda77216b2"
SRC_URI[sha256sum] = "c132d17855584ad61c6e00f3ff11146499755944afc400cce9eae0ecf03d04a6"

S = "${WORKDIR}/pytools-${PV}"

RDEPENDS_${PN} += "\
    python3-decorator \
    python3-appdirs \
    python3-six \
    python3-numpy \
"

inherit pypi setuptools3
