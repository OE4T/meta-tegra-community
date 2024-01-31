SUMMARY = "Convert strings (and dictionary keys) between snake case, camel \
  case and pascal case in Python. Inspired by Humps for Node"
HOMEPAGE = "https://github.com/nficano/humps"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=1d267ceb3a8d8f75f1be3011ee4cbf53"

SRC_URI[sha256sum] = "55e37f16846eaab26057200924cbdadd2152bf0a5d49175a42358464fa881c73"

S = "${WORKDIR}/pyhumps-${PV}"

inherit pypi setuptools3
