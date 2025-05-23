DESCRIPTION = "Dear ImGui: Bloat-free Graphical User interface for C++ with minimal dependencies"
HOMEPAGE = "https://github.com/ocornut/imgui"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE.txt;md5=2c45028b0f14d9585774c12615de461b"

SRC_URI = " \
    git://github.com/ocornut/imgui.git;protocol=https;nobranch=1 \
    file://0001-add-ImGuiContext-structure-and-define-GImGui-variabl.patch \
"

SRCREV = "f3373780668fba1f9bd64c208d05c20b781c9a39"

PV = "1.88+git"

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
