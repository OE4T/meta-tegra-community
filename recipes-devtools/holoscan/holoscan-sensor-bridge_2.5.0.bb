SUMMARY = "NVIDIA Holoscan Sensor Bridge - Bring Your Own Sensor (BYOS) over Ethernet"
HOMEPAGE = "https://github.com/nvidia-holoscan/holoscan-sensor-bridge"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=86d3f3a95c324c9479bd8986968f4327"

SRC_URI = "git://github.com/nvidia-holoscan/holoscan-sensor-bridge.git;protocol=https;nobranch=1;tag=${PV}"
SRCREV = "903f3db376a13ff6a2d4221b388931f87f5632b5"

DEPENDS += "cuda-cudart tegra-libraries-camera tegra-libraries-nvsci holoscan-sdk curl python3-pybind11 libeigen nvtx imgui sipl"

inherit cmake cuda setuptools3-base

SRC_URI:append = " \
    file://0001-Updates-for-OE-cross-builds.patch \
    file://0002-extract-the-CUDA-kernel-from-the-image-processor.patch \
    file://0003-docs-use-ip-instead-of-nmcli.patch \
"

PACKAGECONFIG ??= "fusa coe tools examples test"
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

do_install:append() {
    rm -rf ${D}/usr/scripts
}

INSANE_SKIP:${PN} += "buildpaths"
INSANE_SKIP:${PN}-staticdev += "buildpaths"
INSANE_SKIP:${PN}-dbg += "buildpaths"
INSANE_SKIP:${PN}-dev += "buildpaths"
