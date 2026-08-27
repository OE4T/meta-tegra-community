DESCRIPTION = "Dear ImGui: Bloat-free Graphical User interface for C++ with minimal dependencies"
HOMEPAGE = "https://github.com/ocornut/imgui"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE.txt;md5=619e1c19423c364c1f844b67eb0a9941"

SRC_URI = "git://github.com/ocornut/imgui.git;protocol=https;nobranch=1;tag=v${PV}"

SRCREV = "458a1090314a965dd37b02c918d83077a0142ad5"

do_configure[noexec] = "1"
do_compile[noexec] = "1"

do_install() {
    install -d ${D}/opt/nvidia/imgui
    cp -R --preserve=mode,links,timestamps ${S}/* ${D}/opt/nvidia/imgui
}

ALLOW_EMPTY:${PN} = "1"

FILES:${PN}-dev += "/opt/nvidia/imgui"
RDEPENDS:${PN}-dev += "bash"

SYSROOT_DIRS:append = " /opt/nvidia/imgui"
