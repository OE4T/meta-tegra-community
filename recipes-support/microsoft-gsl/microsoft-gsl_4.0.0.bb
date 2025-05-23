DESCRIPTION = "Guidelines Support Library."
HOMEPAGE = "https://github.com/microsoft/GSL"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=363055e71e77071107ba2bb9a54bd9a7"

SRC_URI = " \
    git://github.com/microsoft/GSL.git;protocol=https;branch=main \
    file://0001-Fixups-for-cross-building-in-OE.patch \
"
# tag: v4.0.0
SRCREV = "a3534567187d2edc428efd3f13466ff75fe5805c"
PV .= "+git"

S = "${UNPACKDIR}/git"

inherit cmake pkgconfig

DEPENDS += "googletest"

EXTRA_OECMAKE = "-DGSL_INSTALL=ON"
