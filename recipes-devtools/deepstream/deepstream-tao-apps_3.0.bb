DESCRIPTION = "NVIDIA Train, Adapt, and Optimize (TAO) Toolkit sample applications"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE.md;md5=dbef1fb16cd9e5c5249a8e0c5e639fb0"

DS_VERSION = "6.0"
DEPENDS = " deepstream-${DS_VERSION}"

SRC_URI = " \
    git://github.com/NVIDIA-AI-IOT/deepstream_tao_apps.git;branch=release/tao3.0_ds6.0ga \
    file://0001-Cross-build-fixups.patch \
"
SRCREV = "2022175c07b7c76437f1e7a10ae90eb8517baf73"

S = "${WORKDIR}/git"

inherit pkgconfig cuda
COMPATIBLE_MACHINE = "(tegra)"

EXTRA_OEMAKE += "CC='${CXX}' CUDA_VER=${CUDA_VERSION} DS_VER=${DS_VERSION} TARGET_DEVICE=${TARGET_ARCH}"

TAO_SAMPLES_PATH = "${bindir}/tao_samples"

do_install () {
    install -d ${D}${libdir}
    install -m 0755 ${S}/post_processor/libnvds_infercustomparser_tao.so ${D}${libdir}/libnvds_infercustomparser_tao.so.${PV}

    install -d ${D}${TAO_SAMPLES_PATH}
    install -m 0755 ${S}/apps/tao_detection/ds-tao-detection ${D}${TAO_SAMPLES_PATH}
    install -m 0755 ${S}/apps/tao_segmentation/ds-tao-segmentation ${D}${TAO_SAMPLES_PATH}
    install -m 0755 ${S}/apps/tao_classifier/ds-tao-classifier ${D}${TAO_SAMPLES_PATH}
}

PACKAGES += "${PN}-samples ${PN}-custom-parser"
FILES_${PN} = ""
ALLOW_EMPTY_${PN} = "1"
FILES_${PN}-custom-parser = "${libdir}"
FILES_${PN}-samples = "${TAO_SAMPLES_PATH}"
RDEPENDS_${PN} += "${PN}-samples ${PN}-custom-parser"
