SUMMARY = "NVIDIA CUDA Deep Neural Network samples"
HOMEPAGE = "https://developer.nvidia.com/cudnn"
LICENSE = "Proprietary"
LIC_FILES_CHKSUM = "file://RNN_v8.0/RNN_example.h;endline=10;md5=279f10cad80a69894c566bd5319e2a03"

inherit l4t_deb_pkgfeed

L4T_DEB_GROUP = "cudnn-samples"

DEPENDS = "cudnn cuda-cudart libcublas freeimage"

SRC_COMMON_DEBS = "\
    libcudnn9-samples_${PV}_all.deb;name=samples;subdir=${BPN}-${PV} \
"
SRC_URI[samples.sha256sum] = "674152f1fa5ca91cacbb0c74ffaec9550bf6d46e45cd92266e8e35193eefc97b"

SRC_URI:append = " file://0001-Update-Makefile-for-OE-compatibility.patch"

COMPATIBLE_MACHINE = "(tegra)"

inherit pkgconfig cmake cuda

S = "${UNPACKDIR}/${BPN}-${PV}/usr/src/cudnn_samples_v9"

CUDNN_SAMPLES_INSTALL_PATH = "${bindir}/cudnn-samples"
EXTRA_OECMAKE:append = " -DCMAKE_INSTALL_CUDNN_SAMPLES=${CUDNN_SAMPLES_INSTALL_PATH}"

INSANE_SKIP:${PN} += "buildpaths"
PACKAGE_ARCH = "${TEGRA_PKGARCH}"
