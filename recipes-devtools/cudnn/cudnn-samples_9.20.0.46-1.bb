SUMMARY = "NVIDIA cuDNN sample applications"
HOMEPAGE = "https://developer.nvidia.com/cudnn"
LICENSE = "LicenseRef-Proprietary"
LIC_FILES_CHKSUM = "file://usr/share/doc/libcudnn9-samples/copyright;md5=6309a40f44d0c8b8cd8950ef80691561"

inherit l4t_deb_pkgfeed cmake cuda

L4T_DEB_GROUP = "cudnn-samples"

SRC_COMMON_DEBS = "\
    libcudnn9-samples_${PV}_all.deb;name=samples;subdir=cudnn-samples \
"

SRC_URI += "file://0001-mnistCUDNN-use-system-FreeImage-via-find_package.patch;patchdir=usr/src/cudnn_samples_v9"
SRC_URI += "file://0002-mnistCUDNN-add-CUDNN_DATA_PATH-env-var.patch;patchdir=usr/src/cudnn_samples_v9"
SRC_URI += "file://0003-CMakeLists-redirect-install-to-CMAKE_INSTALL_CUDNN_S.patch;patchdir=usr/src/cudnn_samples_v9"

SRC_URI[samples.sha256sum] = "c3d05b2c70d4ae26ba34a4eca0f86482bcc65a2c919306a5739f9eed9976cafc"

DEPENDS:append = " cudnn cuda-cudart cuda-crt freeimage"

COMPATIBLE_MACHINE = "(tegra)"
PACKAGE_ARCH = "${TEGRA_PKGARCH}"

S = "${UNPACKDIR}/cudnn-samples"

OECMAKE_SOURCEPATH = "${S}/usr/src/cudnn_samples_v9"


CUDNN_SAMPLES_INSTALL_PATH = "${bindir}/cudnn-samples"

EXTRA_OECMAKE:append = " -DCMAKE_CUDA_ARCHITECTURES=${OECMAKE_CUDA_ARCHITECTURES}"
EXTRA_OECMAKE:append = " -DCMAKE_INSTALL_CUDNN_SAMPLES=bin/cudnn-samples"

do_install:append() {
    cp -r ${S}/usr/src/cudnn_samples_v9/mnistCUDNN/data ${D}${CUDNN_SAMPLES_INSTALL_PATH}/
    ln -s RNN_v8.0 ${D}${CUDNN_SAMPLES_INSTALL_PATH}/RNN_v8
}

FILES:${PN} = "${CUDNN_SAMPLES_INSTALL_PATH}"

RDEPENDS:${PN} = "cudnn"

INSANE_SKIP:${PN} = "ldflags buildpaths"
INHIBIT_PACKAGE_STRIP = "1"
