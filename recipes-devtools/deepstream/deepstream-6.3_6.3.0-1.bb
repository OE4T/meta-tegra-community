DESCRIPTION = "NVIDIA Deepstream SDK"
HOMEPAGE = "https://developer.nvidia.com/deepstream-sdk"
LICENSE = "Proprietary"
LIC_FILES_CHKSUM = " \
    file://usr/share/doc/deepstream-6.3/copyright;md5=165df2c88b1df616c8599ed83276c92b \
    file://opt/nvidia/deepstream/deepstream-6.3/LICENSE.txt;md5=8defc274ed63a1a47ea251872da763ce \
    file://opt/nvidia/deepstream/deepstream-6.3/doc/nvidia-tegra/LICENSE.iothub_client;md5=4f8c6347a759d246b5f96281726b8611 \
    file://opt/nvidia/deepstream/deepstream-6.3/doc/nvidia-tegra/LICENSE.nvds_amqp_protocol_adaptor;md5=8b4b651fa4090272b2e08e208140a658 \
"

inherit l4t_deb_pkgfeed

SRC_COMMON_DEBS = "${BPN}_${PV}_arm64.deb;subdir=${BPN}"
SRC_URI[sha256sum] = "f3961bc473312d46f5e2568f41b37913cb09a5aef69d905451eaea9cb5ad42cf"

COMPATIBLE_MACHINE = "(tegra)"
PACKAGE_ARCH = "${TEGRA_PKGARCH}"

PACKAGECONFIG ??= ""
PACKAGECONFIG[amqp] = ",,rabbitmq-c"
PACKAGECONFIG[kafka] = ",,librdkafka"
# NB: requires hiredis 1.0.0+
PACKAGECONFIG[redis] = ",,hiredis"
# NB: requires avahi to be built with 'libdns_sd' in PACKAGECONFIG
#     which is not the default
PACKAGECONFIG[nmos] = ",,avahi"
# NB: requires Azure IoT Hub client library Azure IoT SDK
PACKAGECONFIG[azure] = ",,azure-iot-sdk"
# NB: need recipes for these dependencies
PACKAGECONFIG[triton] = ""
PACKAGECONFIG[rivermax] = ""
PACKAGECONFIG[realsense] = ""

DEPENDS = "glib-2.0 gstreamer1.0 gstreamer1.0-plugins-base gstreamer1.0-rtsp-server \
    tensorrt-core tensorrt-plugins libnvvpi2 libcufft libcublas libnpp json-glib \
    openssl111 tegra-libraries-multimedia-ds tegra-libraries-multimedia yaml-cpp-060 \
    grpc protobuf tegra-libraries-nvdsseimeta libgstnvcustomhelper mosquitto jsoncpp \
"
# XXX--- see hack in do_install
DEPENDS += "patchelf-native"
# ---XXX

S = "${WORKDIR}/${BPN}"
B = "${WORKDIR}/build"

DEEPSTREAM_BASEDIR = "/opt/nvidia/deepstream"
DEEPSTREAM_PATH = "${DEEPSTREAM_BASEDIR}/deepstream-6.3"
SYSROOT_DIRS += "${DEEPSTREAM_PATH}/lib/ ${DEEPSTREAM_PATH}/sources/includes/"

do_configure() {
    for feature in azure amqp kafka nmos redis triton rivermax realsense; do
        if ! echo "${PACKAGECONFIG}" | grep -q "$feature"; then
            rm -f ${S}${DEEPSTREAM_PATH}/lib/libnvds_${feature}*
            if [ "$feature" = "azure" ]; then
                rm -f ${D}${DEEPSTREAM_PATH}/lib/libnvds_azure*
            fi
            if [ "$feature" = "nmos" ]; then
                rm -f ${S}${DEEPSTREAM_PATH}/lib/libnvds_nmos.so*
                rm -f ${S}${DEEPSTREAM_PATH}/sources/includes/nvdsnmos.h
                rm -f ${S}${DEEPSTREAM_PATH}/bin/deepstream-nmos-app
                rm -rf ${S}${DEEPSTREAM_PATH}/apps/sample_apps/deepstream-nmos
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
    rm -rf ${S}${DEEPSTREAM_PATH}/sources/libs/gstnvcustomhelper
    rm -f ${S}${DEEPSTREAM_PATH}/sources/includes/gst-nvcustomevent.h
    rm -f ${S}${DEEPSTREAM_PATH}/lib/libiothub_client.so*
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
    cp -R --preserve=mode,timestamps ${S}${DEEPSTREAM_PATH}/sources/ ${D}${DEEPSTREAM_PATH}/

    # XXX---
    # Some of the libraries are not using the right SONAME
    # in its DT_NEEDED ELF header, so we have to rewrite it to prevent
    # a broken runtime dependency.
    grpc_soname=$(${OBJDUMP} -p ${STAGING_LIBDIR}/libgrpc.so | grep SONAME | awk '{print $2}')
    protobuf_soname=$(${OBJDUMP} -p ${STAGING_LIBDIR}/libprotobuf.so | grep SONAME | awk '{print $2}')
    patchelf --replace-needed libcufft.so libcufft.so.10 ${D}${DEEPSTREAM_PATH}/lib/libnvds_nvmultiobjecttracker.so
    patchelf --replace-needed libcublas.so libcublas.so.11 ${D}${DEEPSTREAM_PATH}/lib/libnvds_nvmultiobjecttracker.so
    patchelf --replace-needed libcufft.so libcufft.so.10 ${D}${DEEPSTREAM_PATH}/lib/libnvds_audiotransform.so
    bbnote "Patching libgrpc++ NEEDED to $grpc_soname"
    patchelf --replace-needed libgrpc++.so.1.38 $grpc_soname ${D}${DEEPSTREAM_PATH}/lib/libnvds_riva_tts.so
    patchelf --replace-needed libgrpc++.so.1.38 $grpc_soname ${D}${DEEPSTREAM_PATH}/lib/libnvds_riva_asr_grpc.so
    bbnote "Patching libprotobuf NEEDED to $protobuf_soname"
    patchelf --replace-needed libprotobuf.so.3.15.8.0 $protobuf_soname ${D}${DEEPSTREAM_PATH}/lib/libnvds_riva_asr_grpc.so
    patchelf --replace-needed libprotobuf.so.3.15.8.0 $protobuf_soname ${D}${DEEPSTREAM_PATH}/lib/libnvds_riva_tts.so
    patchelf --replace-needed libprotobuf.so.3.15.8.0 $protobuf_soname ${D}${DEEPSTREAM_PATH}/lib/libnvds_riva_audio_proto.so
    patchelf --replace-needed libnppial.so libnppial.so.11 ${D}${DEEPSTREAM_PATH}/lib/libnvds_vpicanmatch.so
    patchelf --replace-needed libnppist.so libnppist.so.11 ${D}${DEEPSTREAM_PATH}/lib/libnvds_vpicanmatch.so
    patchelf --replace-needed libjsoncpp.so.1 libjsoncpp.so.25 ${D}${DEEPSTREAM_PATH}/lib/libnvds_rest_server.so
    # ---XXX
    cd ${D}${DEEPSTREAM_BASEDIR}
    ln -s deepstream-6.3 deepstream
    cd -
}

INHIBIT_PACKAGE_STRIP = "1"
INHIBIT_PACKAGE_DEBUG_SPLIT = "1"
INHIBIT_SYSROOT_STRIP = "1"
INSANE_SKIP = "dev-so ldflags"

def pkgconf_packages(d):
    pkgconf = bb.utils.filter('PACKAGECONFIG', 'azure amqp kafka nmos redis triton rivermax realsense', d).split()
    pn = d.getVar('PN')
    return ' '.join(['{}-{}'.format(pn, p) for p in pkgconf])

PKGCONF_PACKAGES = "${@pkgconf_packages(d)}"

PACKAGES = "${PN}-samples-data ${PN}-samples ${PN}-dev ${PN}-staticdev ${PN}-sources ${PN}-dbg ${PKGCONF_PACKAGES} ${PACKAGE_BEFORE_PN} ${PN}"

FILES:${PN} = "\
    ${sysconfdir}/ld.so.conf.d/  \
    ${libdir}/gstreamer-1.0/deepstream \
    ${DEEPSTREAM_PATH}/lib \
    ${DEEPSTREAM_BASEDIR} \
"

FILES:${PN}-dev = "${DEEPSTREAM_PATH}/sources/includes"

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

FILES:${PN}-azure = "${DEEPSTREAM_PATH}/lib/libnvds_azure*"
FILES:${PN}-nmos = "${DEEPSTREAM_PATH}/lib/lib/libnvds_nmos*"
FILES:${PN}-triton = "\
    ${libdir}/gstreamer-1.0/deepstream/libnvdsgst_inferserver.so \
    ${DEEPSTREAM_PATH}/lib/libnvds_infer_server.so \
"
FILES:${PN}-amqp = "${DEEPSTREAM_PATH}/lib/libnvds_amqp*"
FILES:${PN}-kafka = "${DEEPSTREAM_PATH}/lib/libnvds_kafka*"
FILES:${PN}-redis = "${DEEPSTREAM_PATH}/lib/libnvds_redis*"
FILES:${PN}-rivermax = "${libdir}/gstreamer-1.0/deepstream/libnvdsgst_udp.so"
FILES:${PN}-realsense = "${DEEPSTREAM_PATH}/lib/libnvds_3d_dataloader_realsense.so"
FILES:${PN}-staticdev = "${libdir}/gstreamer-1.0/deepstream/libnvdsgst_multistream_legacy.a"

RDEPENDS:${PN} = "libgstnvcustomhelper"
RDEPENDS:${PN}-samples = "${PN}-samples-data libgstnvcustomhelper"
RDEPENDS:${PN}-samples-data = "bash"
RDEPENDS:${PN}-sources = "bash ${PN}-samples-data ${PN}"
RRECOMMENDS:${PN}-sources += "${PN}-dev"
RRECOMMENDS:${PN} = "liberation-fonts"
