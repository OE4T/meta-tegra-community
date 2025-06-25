SUMMARY = "Fast, re-entrant optimistic lock implemented in Cython"
HOMEPAGE = "https://github.com/scoder/fastrlock"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=452cbb4febc674b7cc090bb3418f4377"

PYPI_PACKAGE = "fastrlock"

inherit pypi setuptools3

SRC_URI[sha256sum] = "4af6734d92eaa3ab4373e6c9a1dd0d5ad1304e172b1521733c6c3b3d73c8fa5d"

DEPENDS += "python3-cython-native"

BBCLASSEXTEND = "native nativesdk"
