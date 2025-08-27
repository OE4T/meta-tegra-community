SUMMARY = "FreeImage supports popular graphics image formats like PNG, BMP, JPEG, TIFF and others"
HOMEPAGE = "https://freeimage.sourceforge.io/"
LICENSE = "GPL-2.0-or-later & GPL-3.0-or-later & FreeImage"
LIC_FILES_CHKSUM = " \
    file://license-fi.txt;md5=7d2690b4d6d7dd53d69a773664bc4850 \
    file://license-gplv3.txt;md5=e9661e0bea741d71a430b23475da519e \
    file://license-gplv2.txt;md5=0440c487be0c0200c36caf975ab31174 \
"

COMPATIBLE_MACHINE = "(tegra)"

SRC_URI = " \
    ${SOURCEFORGE_MIRROR}/freeimage/FreeImage3180.zip \
    file://0001-OE-cross-build-fixups.patch \
"
SRC_URI[sha256sum] = "f41379682f9ada94ea7b34fe86bf9ee00935a3147be41b6569c9605a53e438fd"

S = "${UNPACKDIR}/FreeImage"

inherit pkgconfig dos2unix

EXTRA_OEMAKE = "-C ${S} -f Makefile.gnu"

do_install() {
	oe_runmake install DESTDIR="${D}"
}

INSANE_SKIP:${PN} = "already-stripped"
