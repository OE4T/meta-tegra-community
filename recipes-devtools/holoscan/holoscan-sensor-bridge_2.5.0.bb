SUMMARY = "NVIDIA Holoscan Sensor Bridge - Bring Your Own Sensor (BYOS) over Ethernet"
HOMEPAGE = "https://github.com/nvidia-holoscan/holoscan-sensor-bridge"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=86d3f3a95c324c9479bd8986968f4327"

COMPATIBLE_MACHINE = "(tegra)"

SRC_URI = "git://github.com/nvidia-holoscan/holoscan-sensor-bridge.git;protocol=https;nobranch=1;tag=${PV}"
SRCREV = "903f3db376a13ff6a2d4221b388931f87f5632b5"

DEPENDS += "cuda-cudart tegra-libraries-camera tegra-libraries-nvsci holoscan-sdk curl python3-pybind11 libeigen nvtx imgui sipl"

inherit cmake cuda setuptools3-base features_check

SRC_URI:append = " \
    file://0001-cmake-updates-to-get-build-granularity-based-on-ROCE.patch \
    file://0002-logging-fix-build-issue-when-using-recent-FMT-versio.patch \
    file://0003-hololink-core-networking-fix-build-issue-with-GCC-15.patch \
    file://0004-hololink-operators-image_processor-extract-the-CUDA-.patch \
    file://0005-docs-user_guide-improve-IP-settings-without-Network-.patch \
    file://0006-examples-linux_imx274_player-drop-dependency-with-py.patch \
    file://10-hololink-mgbe0_0.network \
"

PACKAGECONFIG ??= "fusa coe tools examples test socket"
PACKAGECONFIG[fusa] = "-DHOLOLINK_BUILD_FUSA=ON,-DHOLOLINK_BUILD_FUSA=OFF"
PACKAGECONFIG[coe] = "-DHOLOLINK_BUILD_COE=ON,-DHOLOLINK_BUILD_COE=OFF"
PACKAGECONFIG[roce] = "-DHOLOLINK_BUILD_ROCE=ON,-DHOLOLINK_BUILD_ROCE=OFF"
PACKAGECONFIG[socket] = "-DHOLOLINK_BUILD_SOCKET=ON,-DHOLOLINK_BUILD_SOCKET=OFF"
PACKAGECONFIG[examples] = "-DHOLOLINK_BUILD_EXAMPLES=ON, -DHOLOLINK_BUILD_EXAMPLES=OFF"
PACKAGECONFIG[test] = "-DHOLOLINK_BUILD_TESTS=ON,-DHOLOLINK_BUILD_TESTS=OFF"
PACKAGECONFIG[tools] = "-DHOLOLINK_BUILD_TOOLS=ON,-DHOLOLINK_BUILD_TOOLS=OFF"
PACKAGECONFIG[python] = "-DHOLOLINK_BUILD_PYTHON=ON,-DHOLOLINK_BUILD_PYTHON=OFF"

EXTRA_OECMAKE:append = " \
    -DIMGUI_SOURCE_DIR=${RECIPE_SYSROOT}/opt/nvidia/imgui \
"

REQUIRED_DISTRO_FEATURES = "systemd"

do_install:append() {
    install -d ${D}${sysconfdir}/systemd/network
    install -m 0644 ${UNPACKDIR}/10-hololink-mgbe0_0.network ${D}${sysconfdir}/systemd/network/
    install -d ${D}${sysconfdir}/sysctl.d
    echo "net.core.rmem_max = 10420224" > ${D}${sysconfdir}/sysctl.d/99-rmem.conf    
    rm -rf ${D}/usr/scripts
}

FILES:${PN} += " \
    ${sysconfdir}/systemd/network \
    ${sysconfdir}/sysctl.d \
"

INSANE_SKIP:${PN} += "buildpaths"
INSANE_SKIP:${PN}-staticdev += "buildpaths"
INSANE_SKIP:${PN}-dbg += "buildpaths"
INSANE_SKIP:${PN}-dev += "buildpaths"
