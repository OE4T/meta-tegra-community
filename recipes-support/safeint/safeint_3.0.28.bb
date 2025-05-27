DESCRIPTION = "SafeInt is a class library for C++ that manages integer overflows."
HOMEPAGE = "https://github.com/dcleblanc/SafeInt"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=052fd523d0ea51ed2fb75a46627e23bf"

SRC_URI = " \
    git://github.com/dcleblanc/SafeInt.git;protocol=https;nobranch=1 \
    file://0001-Updates-for-OE-cross-builds.patch \
"
#tag: 3.0.28
SRCREV = "4cafc9196c4da9c817992b20f5253ef967685bf8"

S = "${UNPACKDIR}/git"

inherit cmake

FILES:${PN}-dev += "${datadir}"
