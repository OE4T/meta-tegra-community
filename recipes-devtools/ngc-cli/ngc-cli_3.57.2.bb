DESCRIPTION = "NVIDIA GPU Cloud (NGC) CLI"
HOMEPAGE = "https://org.ngc.nvidia.com/setup/installers/cli"
LICENSE = "CLOSED"

BBCLASSEXTEND = "native"
PACKAGES = "${PN}"

def ngc_pkg_arch(d):
    arch = d.getVar('TARGET_ARCH')
    return 'arm64' if arch == 'aarch64' else 'linux'

SRC_URI = "https://api.ngc.nvidia.com/v2/resources/nvidia/ngc-apps/ngc_cli/versions/${PV}/files/ngccli_${@ngc_pkg_arch(d)}.zip"
SHA256SUM:aarch64 = "0ab022b37d35010145815af473f6ef4fdfde0836e776b3433b36367eb27f903d"
SHA256SUM:x86-64 = "5e527b4514185c2d7794fee5f652545cff77a00d7530fd9142df0a3b9bb8501f"
SRC_URI[sha256sum] = "${SHA256SUM}"

S = "${UNPACKDIR}/${BPN}"

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

do_patch[noexec] = "1"
do_configure[noexec] = "1"
do_compile[noexec] = "1"

EXCLUDE_FROM_SHLIBS = "1"
INSANE_SKIP:${PN} += "ldflags already-stripped debug-files"
