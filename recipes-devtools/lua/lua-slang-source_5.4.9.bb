SUMMARY = "Lua 5.4 sources staged for Slang's slang-fiddle generator"
DESCRIPTION = "Lua 5.4 sources staged for source-level inclusion by Slang's slang-fiddle generator"
HOMEPAGE = "https://www.lua.org/"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://lua.h;beginline=499;endline=520;md5=bd40ca17653e280d236bbec3871ff5a7"

SRC_URI = "git://github.com/lua/lua.git;protocol=https;branch=v5.4;tag=v${PV}"
SRCREV = "312b9efaa1061c2c4cad08554dbc1351c3270eef"

do_configure[noexec] = "1"
do_compile[noexec] = "1"

do_install() {
    install -d ${D}${datadir}/slang-lua-source/lua
    install -m 0644 ${S}/*.c ${S}/*.h ${D}${datadir}/slang-lua-source/lua/
}

FILES:${PN} += "${datadir}/slang-lua-source"

BBCLASSEXTEND = "native"
