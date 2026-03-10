SUMMARY = "NVIDIA GXF Core"
HOMEPAGE = "https://docs.nvidia.com/holoscan/sdk-user-guide/gxf/doc/index.html"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://gxf-install/include/gxf/core/gxf.h;endline=16;md5=f3ad46af80e57edcddfce5c9aaedd6c5"

COMPATIBLE_MACHINE = "(cuda)"

GXF_VERSION = "${@d.getVar('PV').replace('-', '_')}"
GXF_PACKAGE = "gxf_${GXF_VERSION}_holoscan-sdk-cu13_${TARGET_ARCH}"
SRC_URI = "https://edge.urm.nvidia.com/artifactory/sw-holoscan-thirdparty-generic-local/gxf/${GXF_PACKAGE}.tar.gz;subdir=${GXF_PACKAGE}"
SRC_URI[sha256sum] = "555f60c0db9e9469dc6082a773e85c826be8b40d18fea2d2a6b3c90d503b3a32"

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
