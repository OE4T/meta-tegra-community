SUMMARY = "C parser and ctypes automation for python"
HOMEPAGE = "https://pyclibrary.readthedocs.io/en/latest/"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=3e0779d1fc89e60083c1d63c61507990"

SRC_URI[sha256sum] = "9902fffe361bb86f57ab62aa4195ec4dd382b63c5c6892be6d9784ec0a3575f7"

S = "${WORKDIR}/pyclibrary-${PV}"

inherit pypi setuptools3

BBCLASSEXTEND = "native nativesdk"
