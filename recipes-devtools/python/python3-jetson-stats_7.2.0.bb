SUMMARY = "Interactive system-monitor process viewer for NVIDIA Jetson \
  Thor, Orin, Xavier, Nano and TX series"
HOMEPAGE = "https://pypi.org/project/jetson-stats/"
SECTION = "devel/python"
LICENSE = "AGPL-3.0-only"
LIC_FILES_CHKSUM = "file://LICENSE;md5=8763b57f0092c337eb12c354870a324a"

# PEP 625 sdist uses underscores: jetson_stats-7.2.0.tar.gz
PYPI_PACKAGE = "jetson_stats"
SRC_URI += "file://0001-setup.py-strip-host-service-install-for-OE-packaging.patch"
SRC_URI[sha256sum] = "b28e0eba26b96d56a610cbbfbcb5d3d2b74ce22c70d9f4d1b090f7f5f9ff44b9"

COMPATIBLE_MACHINE = "(tegra)"

inherit pypi python_setuptools_build_meta systemd useradd

do_install:append() {
    install -d ${D}${sysconfdir}/profile.d
    install -m 0755 ${D}${datadir}/jetson_stats/jtop_env.sh ${D}${sysconfdir}/profile.d

    install -d ${D}${systemd_system_unitdir}
    install -m 0644 ${D}${datadir}/jetson_stats/jtop.service ${D}${systemd_system_unitdir}
    sed -i 's|ExecStart=/usr/local/bin/jtop|ExecStart=/usr/bin/jtop|g' \
        ${D}${systemd_system_unitdir}/jtop.service

    rm ${D}${bindir}/jetson_config
    rm ${D}${bindir}/jetson_swap
    rm ${D}${bindir}/jetson_release
    rm ${D}${datadir}/jetson_stats/jtop.service
    rm ${D}${datadir}/jetson_stats/jtop_env.sh
    rm -rf ${D}${datadir}/jetson_stats
    # Wheel data-files left an empty ${datadir}; drop it so QA does not
    # complain about an installed-but-unshipped directory.
    rmdir ${D}${datadir} 2>/dev/null || true
}

SYSTEMD_SERVICE:${PN} = "jtop.service"

USERADD_PACKAGES = "${PN}"
USERADD_PARAM:${PN} = "-r -d ${libexecdir} -M -s ${base_sbindir}/nologin -g jtop jtop"
GROUPADD_PARAM:${PN} = "-f -r jtop"

FILES:${PN} += "${sysconfdir}/profile.d"
RDEPENDS:${PN} += " \
    bash \
    python3-ctypes \
    python3-curses \
    python3-email \
    python3-fcntl \
    python3-json \
    python3-multiprocessing \
    python3-smbus2 \
    python3-distro \
    python3-nvidia-ml-py \
    tegra-nvpmodel \
    tegra-tools-jetson-clocks \
    tegra-tools-tegrastats \
    nv-tegra-release \
"
RRECOMMENDS:${PN} += "kernel-module-nvgpu"
