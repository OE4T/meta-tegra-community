SUMMARY = "Single-file zlib-replacement library"
DESCRIPTION = "Single C source file zlib-replacement library with a compatible API"
HOMEPAGE = "https://github.com/richgel999/miniz"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=292e2564dd2edd7c3ee1eb1e50480e25"

SRC_URI = "git://github.com/richgel999/miniz.git;protocol=https;branch=master;tag=${PV}"
SRCREV = "77d0dce8627735138c51770d1799a1ef48f2117d"

inherit cmake

EXTRA_OECMAKE = " \
    -DBUILD_SHARED_LIBS=ON \
    -DBUILD_EXAMPLES=OFF \
    -DBUILD_TESTS=OFF \
    -DBUILD_FUZZERS=OFF \
    -DBUILD_HEADER_ONLY=OFF \
    -DAMALGAMATE_SOURCES=OFF \
    -DINSTALL_PROJECT=ON \
"

BBCLASSEXTEND = "native nativesdk"
