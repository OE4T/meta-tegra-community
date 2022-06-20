DESCRIPTION = "Create extremely-fast and secure embedded HTTP servers with ease."
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=68e2a80f5f9020a66f4512962817cd66"

SRC_URI = " \
    git://github.com/Yellow-Camper/libevhtp.git;protocol=https;branch=master \
    file://0001-fix-cmake-build.patch \
"

SRCREV = "e200bfa85bf253e9cfe1c1a9e705fccb176b9171"

S = "${WORKDIR}/git"

DEPENDS = "libevent"

PACKAGECONFIG ??= "pthreads"
PACKAGECONFIG[openssl] = "-DEVHTP_DISABLE_SSL=OFF,-DEVHTP_DISABLE_SSL=ON,openssl"
PACKAGECONFIG[onig] = "-DEVHTP_DISABLE_REGEX=OFF,-DEVHTP_DISABLE_REGEX=ON,onig"
PACKAGECONFIG[pthreads] = "-DEVHTP_DISABLE_EVTHR=OFF,-DEVHTP_DISABLE_EVTHR=ON"

inherit pkgconfig cmake
