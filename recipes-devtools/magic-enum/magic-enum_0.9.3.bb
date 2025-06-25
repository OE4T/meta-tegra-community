DESCRIPTION = "A library that provides static reflection for enums, work with any enum type without any macro or boilerplate code."
HOMEPAGE = "https://github.com/Neargye/magic_enum"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=b15f48588464ec8ef87d2b560aad2caa"

SRC_URI = "git://github.com/Neargye/magic_enum.git;protocol=https;branch=master"
# tag: v0.9.3
SRCREV = "e1ea11a93d0bdf6aae415124ded6126220fa4f28"
PV .= "+git${SRCPV}"

S = "${UNPACKDIR}/git"

inherit cmake

FILES:${PN}-dev += "${datadir}"
