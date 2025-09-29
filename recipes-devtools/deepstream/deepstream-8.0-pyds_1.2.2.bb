DESCRIPTION = "Python bindings for Deepstream-7.0"
HOMEPAGE = "https://github.com/NVIDIA-AI-IOT/deepstream_python_apps"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=7a01a47514ea2d404b8db41b6cfe6db0"

SRC_REPO = "github.com/NVIDIA-AI-IOT/deepstream_python_apps.git;protocol=https;tag=v${PV}"
SRCBRANCH = "master"
SRC_URI = "git://${SRC_REPO};branch=${SRCBRANCH} \
           file://0001-OE-cross-build-fixups.patch \
           file://0002-Allow-apps-to-be-run-from-other-working-directories.patch \
           "
SRCREV = "6fdeefb7128435873f7794d2242ed48a1471ad7e"

COMPATIBLE_MACHINE = "(tegra)"

DEPENDS = "deepstream-8.0 python3-pybind11 gstreamer1.0-python gstreamer1.0 glib-2.0"
DS_PATH = "/opt/nvidia/deepstream/deepstream-8.0"

inherit cmake python_setuptools_build_meta pkgconfig ptest

OECMAKE_SOURCEPATH = "${S}/bindings"
PEP517_SOURCE_PATH = "${S}/bindings"
SETUPTOOLS_SETUP_PATH = "${B}"
EXTRA_OECMAKE = "-DDS_PATH=${STAGING_DIR_HOST}${DS_PATH} -DPYBIND11_INCLUDE_DIR=${STAGING_INCDIR}/pybind11"

do_compile() {
    if [ -e ${WORKDIR}/site-file.cmake ] ; then
        oecmake_sitefile="-C ${WORKDIR}/site-file.cmake"
    else
        oecmake_sitefile=
    fi
    export CMAKE_GENERATOR="${OECMAKE_GENERATOR}"
    export CMAKE_ARGS="$oecmake_sitefile ${OECMAKE_SOURCEPATH} ${OECMAKE_ARGS} ${EXTRA_OECMAKE}"
    export VERBOSE=1
    python_pep517_do_compile
}

do_install() {
    python_pep517_do_install
    install -d ${D}${DS_PATH}/sources/deepstream_python_apps
    cp -Rf --preserve=mode,timestamps ${S}/apps ${D}${DS_PATH}/sources/deepstream_python_apps/
    find ${D}${DS_PATH}/sources/deepstream_python_apps/apps -type f -name '*config.txt' |
	xargs sed -i -r -e's,=(\.\./)+samples/,=${DS_PATH}/samples/,' \
	      -e's,^ll-config-file=[[:space:]]*(.*)$,ll-config-file=${DS_PATH}/samples/configs/deepstream-app/\1,' \
	      -e's,^ll-lib-file=[[:space:]]*/opt.*/(lib/.*),ll-lib-file=${DS_PATH}/\1,'
}

PACKAGES += "${PN}-samples"
RDEPENDS:${PN} = "python3-pygobject gstreamer1.0-python python3-cuda"
FILES:${PN}-samples = "${DS_PATH}/sources/deepstream_python_apps"
RDEPENDS:${PN}-samples = "${PN} deepstream-8.0-samples-data python3-opencv python3-numpy gobject-introspection"
PACKAGE_ARCH = "${TEGRA_PKGARCH}"
