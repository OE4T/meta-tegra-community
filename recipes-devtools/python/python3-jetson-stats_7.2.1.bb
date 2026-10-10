SUMMARY = "Interactive system-monitor process viewer for NVIDIA Jetson \
  Thor, Orin, Xavier, Nano and TX series"
HOMEPAGE = "https://pypi.org/project/jetson-stats/"
SECTION = "devel/python"
LICENSE = "AGPL-3.0-only"
LIC_FILES_CHKSUM = "file://LICENSE;md5=8763b57f0092c337eb12c354870a324a"

SRC_URI[sha256sum] = "90f74f817a2327d001fc0e9e2a348b78b048b718567a4236b3207a6b756f4d0f"

SRC_URI += "\
            file://0001-setup.py-strip-host-service-install-for-OE-packaging.patch \
            file://0002-Remove-auto-installation-of-systemd-service.patch \
            file://0003-Update-NVIDIA_JETPACK-with-new-versions.patch \
           "

COMPATIBLE_MACHINE = "(tegra)"

inherit pypi python_setuptools_build_meta systemd useradd

DEPENDS += "python3-wheel-native"

do_install:append() {
    install -d ${D}${sysconfdir}/profile.d
    install -m 0755 ${D}${datadir}/jetson_stats/jtop_env.sh ${D}${sysconfdir}/profile.d

    install -d ${D}${systemd_system_unitdir}
    install -m 0644 ${D}${datadir}/jetson_stats/jtop.service ${D}${systemd_system_unitdir}
    sed -i 's|ExecStart=/usr/local/bin/jtop|ExecStart=${bindir}/jtop|g' \
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
