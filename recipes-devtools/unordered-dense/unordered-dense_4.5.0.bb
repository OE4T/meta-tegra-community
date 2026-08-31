SUMMARY = "A fast, densely stored hashmap and hashset"
DESCRIPTION = "A fast and densely stored hashmap and hashset based on robin-hood backward shift deletion"
HOMEPAGE = "https://github.com/martinus/unordered_dense"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=1d7f9e1447a1ba175cfd5e005c801e89"

SRC_URI = "git://github.com/martinus/unordered_dense.git;protocol=https;branch=main;tag=v${PV}"
SRCREV = "73f3cbb237e84d483afafc743f1f14ec53e12314"

inherit cmake

# Header-only INTERFACE library: the header and the CMake package both land in
# ${PN}-dev, so the runtime package is legitimately empty.
ALLOW_EMPTY:${PN} = "1"

BBCLASSEXTEND = "native nativesdk"
