SUMMARY = "C++ implementation of a fast hash map and hash set using robin hood hashing"
HOMEPAGE = "https://github.com/Tessil/robin-map"
SECTION = "libs"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=7d9128c23e4bdb36bdceedce604442e0"

SRC_URI = "git://github.com/Tessil/robin-map.git;branch=master;protocol=https"
SRCREV = "bd14e6830a1474fed9d2d03f5c3b0683d818d540"

inherit cmake

# header-only C++ library, main package is empty
ALLOW_EMPTY:${PN} = "1"
RDEPENDS:${PN}-dev = ""

BBCLASSEXTEND = "native nativesdk"
