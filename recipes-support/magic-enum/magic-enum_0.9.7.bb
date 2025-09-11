SUMMARY = "Static reflection for enums for modern C++"
HOMEPAGE = "https://github.com/Neargye/magic_enum"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=7e7717cf723eb72f57e80fdb651cb318"

SRC_URI = "git://github.com/Neargye/magic_enum.git;protocol=https;nobranch=1;tag=v${PV}"
SRCREV = "e046b69a3736d314fad813e159b1c192eaef92cd"

inherit cmake

FILES:${PN}-dev += "${datadir}"
