SUMMARY = "NVIDIA GXF Core"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://gxf-install/include/gxf/core/gxf.h;endline=16;md5=ffcd9e991308047ac45f73c0d6b7dea0"

COMPATIBLE_MACHINE = "(cuda)"

GXF_VERSION = "${@d.getVar('PV').replace('-', '_')}"
GXF_PACKAGE = "gxf_${GXF_VERSION}_holoscan-sdk-cu13_${TARGET_ARCH}"
SRC_URI = "https://edge.urm.nvidia.com/artifactory/sw-holoscan-thirdparty-generic-local/gxf/${GXF_PACKAGE}.tar.gz;subdir=${GXF_PACKAGE}"
SRC_URI[sha256sum] = "d4d78d7f464c464114e46aa1ffb993820f306439220b01713d82d563cd036703"

SRC_URI += " \
    file://0001-OE-cross-build-fixups.patch \
"

S = "${UNPACKDIR}/${GXF_PACKAGE}"

INSTALL_PATH = "${base_prefix}/opt/nvidia/gxf"

do_install () {
    install -d ${D}${INSTALL_PATH}
    cp -rd --no-preserve=ownership ${S}/gxf-install/bin ${D}${INSTALL_PATH}
    cp -rd --no-preserve=ownership ${S}/gxf-install/include ${D}${INSTALL_PATH}
    cp -rd --no-preserve=ownership ${S}/gxf-install/lib ${D}${INSTALL_PATH}
}

# The GXF library is built for and used exclusively by the Holoscan SDK, and it
# is installed to the target as part of the Holoscan SDK installation (see the
# `holoscan-sdk` recipe). This gxf-core recipe is therefore responsible for
# just fetching and extracting the package to make it available during the
# Holoscan SDK installation (where all additional installation steps and
# bitbake dependency checking is performed).

PACKAGES = "${PN} ${PN}-dev"

FILES:${PN}-dev += " \
    ${INSTALL_PATH} \
"

SYSROOT_DIRS = " \
    ${INSTALL_PATH} \
"

EXCLUDE_FROM_SHLIBS = "1"
INHIBIT_PACKAGE_DEBUG_SPLIT = "1"

INSANE_SKIP:${PN}-dev += "dev-elf file-rdeps staticdev"
