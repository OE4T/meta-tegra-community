SUMMARY = "NVIDIA Holoscan Sensor Bridge - Bring Your Own Sensor (BYOS) over Ethernet"
HOMEPAGE = "https://github.com/nvidia-holoscan/holoscan-sensor-bridge"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=86d3f3a95c324c9479bd8986968f4327"

COMPATIBLE_MACHINE = "(tegra)"

SRC_URI = "\
    git://github.com/nvidia-holoscan/holoscan-sensor-bridge.git;protocol=https;nobranch=1;tag=${PV} \
    file://0001-Updates-for-OE-cross-builds.patch \
    file://10-hololink-mgbe0_0.network \
"
SRCREV = "f54091ba594bc936ade41dd78a531d82724d842e"

DEPENDS += " \
    tegra-libraries-camera \
    tegra-libraries-nvsci \
    holoscan-sdk \
    nvcomp \
"

inherit cmake cuda setuptools3-base features_check

PACKAGECONFIG ??= "socket fusa examples"
PACKAGECONFIG[socket] = "-DHOLOLINK_BUILD_SOCKET=ON,-DHOLOLINK_BUILD_SOCKET=OFF"
PACKAGECONFIG[fusa] = "-DHOLOLINK_BUILD_FUSA=ON,-DHOLOLINK_BUILD_FUSA=OFF,jetson-sipl-api"
PACKAGECONFIG[roce] = "-DHOLOLINK_BUILD_ROCE=ON,-DHOLOLINK_BUILD_ROCE=OFF,libibverbs1 ibverbs-providers"
PACKAGECONFIG[examples] = "-DHOLOLINK_BUILD_EXAMPLES=ON, -DHOLOLINK_BUILD_EXAMPLES=OFF"
PACKAGECONFIG[tests] = "-DHOLOLINK_BUILD_TESTS=ON,-DHOLOLINK_BUILD_TESTS=OFF"
PACKAGECONFIG[tools] = "-DHOLOLINK_BUILD_TOOLS=ON,-DHOLOLINK_BUILD_TOOLS=OFF,curl"
PACKAGECONFIG[python] = "-DHOLOLINK_BUILD_PYTHON=ON,-DHOLOLINK_BUILD_PYTHON=OFF"

# HSB: Holoscan Sensor Bridge
HSB_INSTALL_PATH = "/opt/nvidia/hololink"

EXTRA_OECMAKE:append = " \
    -DL4T_MAJ_VER=39 \
    -DCMAKE_INSTALL_PREFIX=${HSB_INSTALL_PATH} \
    -DIMGUI_SOURCE_DIR=${RECIPE_SYSROOT}/opt/nvidia/imgui \
"

REQUIRED_DISTRO_FEATURES = "systemd"

do_install:append() {
    if "${@bb.utils.contains('PACKAGECONFIG', 'socket', 'true', 'false', d)}"; then
        install -d ${D}${sysconfdir}/sysctl.d
        echo "net.core.rmem_max = 31326208" > ${D}${sysconfdir}/sysctl.d/52-hololink-rmem_max.conf
    fi

    if "${@bb.utils.contains('PACKAGECONFIG', 'fusa', 'true', 'false', d)}"; then
        install -d ${D}${sysconfdir}/systemd/network
        install -m 0644 ${UNPACKDIR}/10-hololink-mgbe0_0.network ${D}${sysconfdir}/systemd/network/
    fi
    install -d ${D}${HSB_INSTALL_PATH}/config
    install -m 0644 ${S}/examples/example_configuration.yaml ${D}${HSB_INSTALL_PATH}/config
    rm -rf ${D}${HSB_INSTALL_PATH}/scripts
}



FILES:${PN} += " \
    ${HSB_INSTALL_PATH} \
    ${@bb.utils.contains('PACKAGECONFIG', 'fusa', '${sysconfdir}/systemd/network', '', d)} \
    ${@bb.utils.contains('PACKAGECONFIG', 'socket', '${sysconfdir}/sysctl.d', '', d)} \
"

FILES:${PN}-dev += " \
    ${HSB_INSTALL_PATH}/include \
    ${HSB_INSTALL_PATH}/lib/cmake \
"

FILES:${PN}-staticdev += " \
    ${HSB_INSTALL_PATH}/lib/*.a \
"

SYSROOT_DIRS = " \
    ${HSB_INSTALL_PATH} \
"

RDEPENDS:${PN} += "${@bb.utils.contains('PACKAGECONFIG', 'roce', 'ethtool bash', '', d)}"

INSANE_SKIP:${PN} += "buildpaths"
INSANE_SKIP:${PN}-staticdev += "buildpaths"
INSANE_SKIP:${PN}-dbg += "buildpaths"
INSANE_SKIP:${PN}-dev += "buildpaths"
