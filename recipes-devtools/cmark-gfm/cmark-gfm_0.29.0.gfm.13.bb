SUMMARY = "CommonMark library with GitHub Flavored Markdown extensions"
DESCRIPTION = "CommonMark parsing and rendering library with GitHub Flavored Markdown extensions (swiftlang fork)"
HOMEPAGE = "https://github.com/swiftlang/swift-cmark"
LICENSE = "BSD-2-Clause AND MIT"
LIC_FILES_CHKSUM = "file://COPYING;md5=81f9cae6293cc0345a9144b78152ab62"

SRC_URI = "git://github.com/swiftlang/swift-cmark.git;protocol=https;branch=gfm"
SRCREV = "924936d0427cb25a61169739a7660230bffa6ea6"

inherit cmake

EXTRA_OECMAKE = " \
    -DCMAKE_BUILD_TYPE=Release \
    -DBUILD_SHARED_LIBS=ON \
    -DBUILD_TESTING=OFF \
    -DCMARK_LIB_FUZZER=OFF \
    -DCMARK_FUZZ_QUADRATIC=OFF \
    -DCMARK_THREADING=OFF \
"

# The exported CMake package declares the cmark-gfm tool as an imported
# executable, and CMake fails find_package() when the file is missing.
SYSROOT_DIRS += "${bindir}"

BBCLASSEXTEND = "native nativesdk"
