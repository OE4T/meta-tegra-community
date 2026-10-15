SUMMARY = "NVIDIA Holoscan Camera: V4L2, SIPL and Argus capture operators for Holoscan SDK"
HOMEPAGE = "https://github.com/nvidia-holoscan/holoscan-camera"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=44b1a6d36426b67ca4b9e75c9d3b288b"

SRC_URI = " \
    git://github.com/nvidia-holoscan/holoscan-camera.git;nobranch=1;protocol=https;tag=v${PV} \
    file://0001-Updates-for-OE-cross-builds.patch \
    file://run-ptest \
"
SRCREV = "a08c18fe7a46e97f636386b7a6cd298f252c6b8f"

COMPATIBLE_MACHINE = "(tegra)"

inherit cmake cuda python3targetconfig ptest features_check l4t_version

DEPENDS += "holoscan-sdk flatbuffers-native"

# Upstream does not support Argus on Thor.
HOLOSCAN_CAMERA_ARGUS ?= ""
HOLOSCAN_CAMERA_ARGUS:tegra234 ?= "argus"
PACKAGECONFIG ??= " \
    v4l2 apps ${HOLOSCAN_CAMERA_ARGUS} \
    ${@bb.utils.contains('DISTRO_FEATURES', 'x11', 'sipl', '', d)} \
    ${@bb.utils.contains('DISTRO_FEATURES', 'ptest', 'tests', '', d)} \
"
PACKAGECONFIG[v4l2] = "-DOP_v4l2_capture_op=ON,-DOP_v4l2_capture_op=OFF"
PACKAGECONFIG[sipl] = "-DOP_sipl_capture_op=ON,-DOP_sipl_capture_op=OFF,jetson-sipl-api tegra-libraries-camera tegra-libraries-nvsci"
PACKAGECONFIG[argus] = "-DOP_argus_capture_op=ON,-DOP_argus_capture_op=OFF,tegra-mmapi tegra-libraries-camera tegra-libraries-multimedia-utils virtual/egl"
PACKAGECONFIG[apps] = ""
PACKAGECONFIG[tests] = "-DHOLOSCAN_CAMERA_BUILD_TESTING=ON,-DHOLOSCAN_CAMERA_BUILD_TESTING=OFF,googletest"
PYTHON_SITEPACKAGES_DIR_NATIVE = "${STAGING_LIBDIR_NATIVE}/python${PYTHON_BASEVERSION}/site-packages"
HOLOSCAN_CAMERA_PYTHON_CMAKE = " \
    -DHOLOSCAN_CAMERA_BUILD_PYTHON=ON \
    -DHOLOSCAN_BUILD_PYTHON_STUBS=OFF \
    -DPython3_EXECUTABLE=${PYTHON} \
    -Dnanobind_ROOT=${PYTHON_SITEPACKAGES_DIR_NATIVE}/nanobind/cmake \
"
PACKAGECONFIG[python] = "${HOLOSCAN_CAMERA_PYTHON_CMAKE},-DHOLOSCAN_CAMERA_BUILD_PYTHON=OFF,python3-nanobind-native"

# jetson-sipl-api needs x11; tegra-libraries-camera needs opengl.
REQUIRED_DISTRO_FEATURES = " \
    ${@bb.utils.contains('PACKAGECONFIG', 'sipl', 'x11', '', d)} \
    ${@bb.utils.contains_any('PACKAGECONFIG', 'sipl argus', 'opengl', '', d)} \
"

def holoscan_camera_apps(d):
    enabled = d.getVar("PACKAGECONFIG").split()
    apps = {
        "holoscan_camera_v4l2": "v4l2",
        "sipl_frame_saver": "sipl",
        "sipl_stereo_monitor": "sipl",
        "sipl_player": "sipl",
        "holoscan_camera_argus": "argus",
    }
    return " ".join("-DAPP_%s=%s" % (app, "ON" if "apps" in enabled and op in enabled else "OFF")
                    for app, op in apps.items())

EXTRA_OECMAKE = " \
    -DHOLOSCAN_CAMERA_BUILD_ALL=OFF \
    -DHOLOSCAN_CAMERA_INSTALL_PACKAGE_CONFIG=ON \
    -DL4T_MAJ_VER=${@'${L4T_VERSION}'.split('.')[0]} \
    ${@holoscan_camera_apps(d)} \
    -DHOLOSCAN_CAMERA_INSTALL_TESTS_DIR=${@os.path.relpath(d.getVar('PTEST_PATH'), d.getVar('prefix'))} \
"
# Module scanning records its build-tree -fdeps-file paths in DW_AT_producer.
EXTRA_OECMAKE += "-DCMAKE_CXX_SCAN_FOR_MODULES=OFF"

do_install:append() {
    if ${@bb.utils.contains('PACKAGECONFIG', 'sipl apps', 'true', 'false', d)}; then
        install -d ${D}${datadir}/holoscan-camera/config/sipl
        install -m 0644 ${S}/applications/config/sipl/*.json ${D}${datadir}/holoscan-camera/config/sipl/
    fi
}

do_install_ptest() {
    DESTDIR='${D}' cmake --install '${B}' --component holoscan-camera-tests
    sed -i -e 's|@BINDIR@|${bindir}|g' ${D}${PTEST_PATH}/run-ptest
}

PACKAGE_BEFORE_PN = "python3-${PN} ${PN}-examples"

FILES:python3-${PN} = "${PYTHON_SITEPACKAGES_DIR}"
RDEPENDS:python3-${PN} += "${PN} python3-holoscan-sdk python3-core"

SUMMARY:${PN}-examples = "Holoscan Camera sample applications"
FILES:${PN}-examples = "${bindir} ${datadir}/holoscan-camera/config"
RDEPENDS:${PN}-examples += "${PN}"

FILES:${PN}-dev += "${datadir}/holoscan-camera/schemas"
# find_dependency(holoscan) and the SIPL headers sipl_capture_service.hpp includes.
RDEPENDS:${PN}-dev += " \
    holoscan-sdk-dev \
    ${@bb.utils.contains('PACKAGECONFIG', 'sipl', 'jetson-sipl-api-dev tegra-nvsci-headers', '', d)} \
"

# libnvsipl loads the camera drivers at run time.
RRECOMMENDS:${PN} += "${@bb.utils.contains('PACKAGECONFIG', 'sipl', 'jetson-sipl-api-drivers', '', d)}"
# USB cameras need the UVC driver, which the Tegra kernels build as a module.
RRECOMMENDS:${PN} += "${@bb.utils.contains('PACKAGECONFIG', 'v4l2', 'kernel-module-uvcvideo', '', d)}"
RRECOMMENDS:${PN}:append:tegra264 = "${@bb.utils.contains('PACKAGECONFIG', 'sipl', ' hsb-config', '', d)}"

RDEPENDS:${PN}-ptest += "coreutils"
RDEPENDS:${PN}-ptest += "${@bb.utils.contains('PACKAGECONFIG', 'apps', '${PN}-examples', '', d)}"
