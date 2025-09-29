DESCRIPTION = "NVIDIA GPU Cloud (NGC) CLI"
HOMEPAGE = "https://org.ngc.nvidia.com/setup/installers/cli"
LICENSE = "CLOSED"

BBCLASSEXTEND = "native"
PACKAGES = "${PN}"

def ngc_pkg_arch(d):
    arch = d.getVar('TARGET_ARCH')
    return 'arm64' if arch == 'aarch64' else 'linux'

SRC_URI = "https://api.ngc.nvidia.com/v2/resources/nvidia/ngc-apps/ngc_cli/versions/${PV}/files/ngccli_${@ngc_pkg_arch(d)}.zip"
SHA256SUM:aarch64 = "d2aae067b82f95cacb5bf514155cc0eea4c2bfba3b951089963ab3ceef625c05"
SHA256SUM:x86-64 = "5f01eff85a66c895002f3c87db2933c462f3b86e461e60d515370f647b4ffc21"
SRC_URI[sha256sum] = "${SHA256SUM}"

S = "${UNPACKDIR}/${BPN}"

do_patch[noexec] = "1"
do_configure[noexec] = "1"
do_compile[noexec] = "1"

INSTALL_PATH = "${base_prefix}/opt/nvidia"

do_install() {
    install -d ${D}${INSTALL_PATH}
    cp -rd --no-preserve=ownership ${S} ${D}${INSTALL_PATH}
    install -d ${D}${bindir}
    ln -s ../../opt/nvidia/ngc-cli/ngc ${D}${bindir}/ngc
}

FILES:${PN} = " \
    ${INSTALL_PATH} \
    ${bindir} \
"

SYSROOT_DIRS_NATIVE += " \
    ${INSTALL_PATH} \
"

RDEPENDS:${PN}:aarch64 = " \
    glibc \
"

EXCLUDE_FROM_SHLIBS = "1"
INHIBIT_PACKAGE_DEBUG_SPLIT = "1"

INSANE_SKIP:${PN} += "ldflags already-stripped"
