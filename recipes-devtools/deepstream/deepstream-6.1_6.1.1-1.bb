DESCRIPTION = "NVIDIA Deepstream SDK"
HOMEPAGE = "https://developer.nvidia.com/deepstream-sdk"
LICENSE = "Proprietary"
LIC_FILES_CHKSUM = " \
    file://usr/share/doc/deepstream-6.1/copyright;md5=32b2256361779ec59211b3a698f24ce2 \
    file://opt/nvidia/deepstream/deepstream-6.1/LICENSE.txt;md5=430d70b62dcd279de697edf2d7a8661e \
    file://opt/nvidia/deepstream/deepstream-6.1/doc/nvidia-tegra/LICENSE.iothub_client;md5=4f8c6347a759d246b5f96281726b8611 \
    file://opt/nvidia/deepstream/deepstream-6.1/doc/nvidia-tegra/LICENSE.nvds_amqp_protocol_adaptor;md5=8b4b651fa4090272b2e08e208140a658 \
"

inherit l4t_deb_pkgfeed

SRC_COMMON_DEBS = "${BPN}_${PV}_arm64.deb;subdir=${BPN}"
SRC_URI[sha256sum] = "5d901325c9ddd8e8e90ce70bce1fb94fa1b6a4749e6d2f26b4f78e235c9658de"

COMPATIBLE_MACHINE = "(tegra)"
PACKAGE_ARCH = "${TEGRA_PKGARCH}"

PACKAGECONFIG ??= ""
PACKAGECONFIG[amqp] = ",,rabbitmq-c"
PACKAGECONFIG[kafka] = ",,librdkafka"
# NB: requires hiredis 1.0.0+
PACKAGECONFIG[redis] = ",,hiredis"
# NB: need recipes for these dependencies
PACKAGECONFIG[azure] = ""
PACKAGECONFIG[triton] = ""
PACKAGECONFIG[rivermax] = ""
PACKAGECONFIG[realsense] = ""

DEPENDS = "glib-2.0 gstreamer1.0 gstreamer1.0-plugins-base gstreamer1.0-rtsp-server \
    tensorrt-core tensorrt-plugins libnvvpi2 libcufft libcublas libnpp json-glib \
    openssl111 tegra-libraries-multimedia yaml-cpp-060 mdns \
"
# XXX--- see hack in do_install
DEPENDS += "patchelf-native"
# ---XXX

S = "${WORKDIR}/${BPN}"
B = "${WORKDIR}/build"

DEEPSTREAM_BASEDIR = "/opt/nvidia/deepstream"
DEEPSTREAM_PATH = "${DEEPSTREAM_BASEDIR}/deepstream-6.1"
SYSROOT_DIRS += "${DEEPSTREAM_PATH}/lib/"

do_configure() {
    for feature in azure amqp kafka redis triton rivermax realsense; do
        if ! echo "${PACKAGECONFIG}" | grep -q "$feature"; then
            rm -f ${S}${DEEPSTREAM_PATH}/lib/libnvds_${feature}*
            if [ "$feature" = "azure" ]; then
                rm -f ${S}${DEEPSTREAM_PATH}/lib/libiothub_client.so
            fi
            if [ "$feature" = "triton" ]; then
                rm -f ${S}${DEEPSTREAM_PATH}/lib/gst-plugins/libnvdsgst_inferserver.so
                rm -f ${S}${DEEPSTREAM_PATH}/lib/libnvds_infer_server.so
            fi
            if [ "$feature" = "rivermax" ]; then
                rm -f ${S}${DEEPSTREAM_PATH}/lib/gst-plugins/libnvdsgst_udp.so
            fi
            if [ "$feature" = "realsense" ]; then
                rm -f ${S}${DEEPSTREAM_PATH}/lib/libnvds_3d_dataloader_realsense.so
            fi
        fi
    done
}

do_install() {
    install -d ${D}${bindir}/
    install -m 0755 ${S}${DEEPSTREAM_PATH}/bin/* ${D}${bindir}/

    install -d ${D}${DEEPSTREAM_PATH}/lib/
    for f in ${S}${DEEPSTREAM_PATH}/lib/*; do
        [ ! -d "$f" ] || continue
        install -m 0644 "$f" ${D}${DEEPSTREAM_PATH}/lib/
    done
    ln -sf libnvds_msgconv.so.1.0.0 ${D}${DEEPSTREAM_PATH}/lib/libnvds_msgconv.so
    ln -sf libnvds_msgconv_audio.so.1.0.0 ${D}${DEEPSTREAM_PATH}/lib/libnvds_msgconv_audio.so

    cp -R --preserve=mode,timestamps ${S}${DEEPSTREAM_PATH}/lib/cvcore_libs/ ${D}${DEEPSTREAM_PATH}/lib/

    install -d ${D}/${sysconfdir}/ld.so.conf.d/
    echo "${DEEPSTREAM_PATH}/lib" > ${D}/${sysconfdir}/ld.so.conf.d/deepstream.conf
    echo "${libdir}/gstreamer-1.0/deepstream" >> ${D}/${sysconfdir}/ld.so.conf.d/deepstream.conf

    install -d ${D}${libdir}/gstreamer-1.0/deepstream
    install -m 0644 ${S}${DEEPSTREAM_PATH}/lib/gst-plugins/* ${D}${libdir}/gstreamer-1.0/deepstream/

    cp -R --preserve=mode,timestamps ${S}${DEEPSTREAM_PATH}/samples ${D}${DEEPSTREAM_PATH}/

    install -d ${D}${includedir}/deepstream
    cp -R --preserve=mode,timestamps ${S}${DEEPSTREAM_PATH}/sources/includes/* ${D}${includedir}/

    cp -R --preserve=mode,timestamps ${S}${DEEPSTREAM_PATH}/sources/ ${D}${DEEPSTREAM_PATH}/

    # XXX---
    # Some of the libraries are not using the right SONAME
    # in its DT_NEEDED ELF header, so we have to rewrite it to prevent
    # a broken runtime dependency.
    patchelf --replace-needed libdns_sd.so.1.0.0 libdns_sd.so.1 ${D}${DEEPSTREAM_PATH}/lib/libnvds_nvmultiobjecttracker.so
    patchelf --replace-needed libdns_sd.so.1.0.0 libdns_sd.so.1 ${D}${DEEPSTREAM_PATH}/lib/libnvds_nmos.so
    patchelf --replace-needed libcufft.so libcufft.so.10 ${D}${DEEPSTREAM_PATH}/lib/libnvds_nvmultiobjecttracker.so
    patchelf --replace-needed libcublas.so libcublas.so.11 ${D}${DEEPSTREAM_PATH}/lib/libnvds_nvmultiobjecttracker.so
    patchelf --replace-needed libcufft.so libcufft.so.10 ${D}${DEEPSTREAM_PATH}/lib/libnvds_audiotransform.so
    # ---XXX
    cd ${D}${DEEPSTREAM_BASEDIR}
    ln -s deepstream-6.1 deepstream
    cd -
}

INHIBIT_PACKAGE_STRIP = "1"
INHIBIT_PACKAGE_DEBUG_SPLIT = "1"
INHIBIT_SYSROOT_STRIP = "1"
INSANE_SKIP = "dev-so ldflags"

def pkgconf_packages(d):
    pkgconf = bb.utils.filter('PACKAGECONFIG', 'azure amqp kafka redis triton rivermax realsense', d).split()
    pn = d.getVar('PN')
    return ' '.join(['{}-{}'.format(pn, p) for p in pkgconf])

PKGCONF_PACKAGES = "${@pkgconf_packages(d)}"

PACKAGES =+ "${PN}-samples-data ${PN}-samples ${PN}-sources ${PKGCONF_PACKAGES}"

FILES:${PN} = "\
    ${sysconfdir}/ld.so.conf.d/  \
    ${libdir}/gstreamer-1.0/deepstream \
    ${DEEPSTREAM_PATH}/lib \
    ${DEEPSTREAM_BASEDIR} \
"

FILES:${PN}-dev = "${includedir}"

FILES:${PN}-samples = "${bindir}/*"
FILES:${PN}-samples-data = "\
    ${DEEPSTREAM_PATH}/samples \
    ${DEEPSTREAM_PATH}/sources/apps/sample_apps/*/*.txt \
    ${DEEPSTREAM_PATH}/sources/apps/sample_apps/*/README \
    ${DEEPSTREAM_PATH}/sources/apps/sample_apps/*/configs/ \
    ${DEEPSTREAM_PATH}/sources/apps/sample_apps/*/inferserver/ \
    ${DEEPSTREAM_PATH}/sources/apps/sample_apps/*/csv_files/ \
"

FILES:${PN}-sources = "${DEEPSTREAM_PATH}/sources"

FILES:${PN}-azure = "${DEEPSTREAM_PATH}/lib/libiothub_client.so ${DEEPSTREAM_PATH}/lib/libnvds_azure*"
FILES:${PN}-triton = "\
    ${libdir}/gstreamer-1.0/deepstream/libnvdsgst_inferserver.so \
    ${DEEPSTREAM_PATH}/lib/libnvds_infer_server.so \
"
FILES:${PN}-amqp = "${DEEPSTREAM_PATH}/lib/libnvds_amqp*"
FILES:${PN}-kafka = "${DEEPSTREAM_PATH}/lib/libnvds_kafka*"
FILES:${PN}-redis = "${DEEPSTREAM_PATH}/lib/libnvds_redis*"
FILES:${PN}-rivermax = "${libdir}/gstreamer-1.0/deepstream/libnvdsgst_udp.so"
FILES:${PN}-realsense = "${DEEPSTREAM_PATH}/lib/libnvds_3d_dataloader_realsense.so"

RDEPENDS:${PN}-samples = "${PN}-samples-data"
RDEPENDS:${PN}-samples-data = "bash"
RDEPENDS:${PN}-sources = "bash ${PN}-samples-data ${PN}"
RRECOMMENDS:${PN} = "liberation-fonts"
