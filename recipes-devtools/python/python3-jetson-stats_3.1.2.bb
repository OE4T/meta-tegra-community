SUMMARY = "Interactive system-monitor process viewer for NVIDIA Jetson Nano, AGX Xavier, TX2, TX1"
HOMEPAGE = "https://pypi.org/project/jetson-stats/"
SECTION = "devel/python"
LICENSE = "AGPL-3.0-only"
LIC_FILES_CHKSUM = "file://LICENSE;md5=8763b57f0092c337eb12c354870a324a"

SRC_URI[sha256sum] = "afab3db7da554ad14df7b7eee4d0bc3338c827625c690dda0ac4ddd6051d315a"
SRC_URI += "file://0001-jtop-fix-no-attribute-issue.patch"

COMPATIBLE_MACHINE = "(tegra)"

inherit pypi setuptools3_legacy systemd useradd

do_install:append() {
    # Need to move the systemd service into the right place manually and adjust
    # the ExecStart path.
    mkdir -p ${D}${systemd_system_unitdir}
    mv ${D}${datadir}/jetson_stats/jetson_stats.service \
        ${D}${systemd_system_unitdir}/jetson_stats.service
    sed -i 's/ExecStart=\/usr\/local\/bin\/jtop/ExecStart=\/usr\/bin\/jtop/g' \
        ${D}${systemd_system_unitdir}/jetson_stats.service

    # Remove stuff that requires dpkg and is aimed at jetpack anyway
    rm ${D}${bindir}/jetson_config
    rm ${D}${bindir}/jetson_swap
    rm ${D}${bindir}/jetson_release
    rm ${D}${datadir}/jetson_stats/jetson_env.sh
}

SYSTEMD_SERVICE:${PN} = "jetson_stats.service"

USERADD_PACKAGES = "${PN}"
USERADD_PARAM:${PN} = "-r -d ${libexecdir} -M -s ${base_sbindir}/nologin -g jetson_stats jetson_stats"
GROUPADD_PARAM:${PN} = "-f -r jetson_stats"

RDEPENDS:${PN} += " \
    bash \
    python3-curses \
    python3-email \
    python3-fcntl \
    python3-json \
    python3-multiprocessing \
    python3-setuptools \
    tegra-nvpmodel \
    tegra-tools-jetson-clocks \
    tegra-tools-tegrastats \
"

RRECOMMENDS:${PN} += "kernel-module-nvgpu"

FILES:${PN} += "${datadir}/jetson_stats"
