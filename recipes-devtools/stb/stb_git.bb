DESCRIPTION = "single-file libraries for C/C++"
HOMEPAGE = "https://github.com/nothings/stb"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=fb994481c55623ef338885280e676f3b"

SRC_URI = "git://github.com/nothings/stb.git;protocol=https;nobranch=1"
SRCREV = "af1a5bc352164740c1cc1354942b1c6b72eacb8a"

PV = "0.0+git"

do_install() {
    install -d ${D}${includedir}/stb
    for file in ${S}/*.h
    do
        install -m 0644 $file ${D}${includedir}/stb/
    done
}

ALLOW_EMPTY:${PN} = "1"
FILES:${PN}-dev += "${includedir}/stb"
