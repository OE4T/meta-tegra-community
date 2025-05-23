DESCRIPTION = "C++11/14/17 std::expected with functional-style extensions"
HOMEPAGE = "https://github.com/TartanLlama/expected"
LICENSE = "CC0-1.0"
LIC_FILES_CHKSUM = "file://COPYING;md5=65d3616852dbf7b1a6d4b53b00626032"

SRC_URI = " \
    git://github.com/TartanLlama/expected.git;protocol=https;branch=master;tag=v${PV} \
    file://0001-Search-for-Catch2-with-find_package.patch \
    file://0002-update-tl-expected-project-version.patch \
"
SRCREV = "292eff8bd8ee230a7df1d6a1c00c4ea0eb2f0362"

inherit cmake

EXTRA_OECMAKE:append = " -DEXPECTED_BUILD_TESTS=OFF"

ALLOW_EMPTY:${PN} = "1"
