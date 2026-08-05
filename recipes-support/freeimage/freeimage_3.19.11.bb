SUMMARY = "FreeImage image loading library"
HOMEPAGE = "https://freeimage.sourceforge.io/"
LICENSE = "GPL-2.0-or-later OR GPL-3.0-or-later OR LicenseRef-FreeImage"
LIC_FILES_CHKSUM = "file://license-fi.txt;md5=8e1438cab62c8f655288588dc43daaf6 \
                    file://license-gplv2.txt;md5=1fbed70be9d970d3da399f33dae9cc51 \
                    file://license-gplv3.txt;md5=b5c176c43d7fb06bf6dd56e79c490f5b"

# danoli3/FreeImage: actively maintained fork with security fixes beyond Debian/Ubuntu
# (CVE-2019-12211-13, BMP/PFM/TARGA/CUT/PICT heap buffer overflow hardening)
# 3.19.11 adds cmake support and fixes C++17 compatibility issues present in 3.18.0
SRC_URI = "git://github.com/danoli3/FreeImage.git;protocol=https;branch=master"
SRCREV = "625117b48e18a82f3f68e9be3514ae584ebfcf7b"

PV = "3.19.11"

inherit cmake

DEPENDS = ""

EXTRA_OECMAKE = "\
    -DBUILD_SHARED_LIBS=ON \
    -DBUILD_OPENEXR=OFF \
    -DBUILD_JXR=OFF \
    -DBUILD_TESTS=OFF \
    -DBUILD_LIBRAWLITE=OFF \
"

FILES:${PN} = "${libdir}/libFreeImage.so"
FILES:${PN}-dev = "${includedir} ${libdir}/cmake"

INSANE_SKIP:${PN} = "dev-so"
