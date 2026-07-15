DESCRIPTION = "NVIDIA Deepstream SDK"
HOMEPAGE = "https://developer.nvidia.com/deepstream-sdk"
LICENSE = "Proprietary"
LIC_FILES_CHKSUM = " \
    file://usr/share/doc/deepstream-9.1/copyright;md5=d491e636a05accc669f7c2741540bc0f \
    file://opt/nvidia/deepstream/deepstream-9.1/LICENSE.txt;md5=675906f1fcfa7711c6b07dc356dfe550 \
    file://opt/nvidia/deepstream/deepstream-9.1/doc/nvidia-tegra/LICENSE.iothub_client;md5=4f8c6347a759d246b5f96281726b8611 \
    file://opt/nvidia/deepstream/deepstream-9.1/doc/nvidia-tegra/LICENSE.nvds_amqp_protocol_adaptor;md5=18387e328467c2223be7763dd34b8ab8 \
"

inherit l4t_deb_pkgfeed

SRC_COMMON_DEBS = "${BPN}_${PV}_arm64.deb;subdir=${BPN}"
SRC_URI[sha256sum] = "7e7a08811c49eb95e779f0c16e1f157ef721ade63aa6a2e3845442be29aab7b4"

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
    tensorrt-core tensorrt-plugins libnvvpi4 libcufft libcublas libnpp json-glib \
    openssl tegra-libraries-multimedia-ds tegra-libraries-multimedia yaml-cpp-080 \
    grpc protobuf tegra-libraries-nvdsseimeta libgstnvcustomhelper mosquitto jsoncpp cuda-nvrtc \
"
# XXX--- see hack in do_install
DEPENDS += "patchelf-native"
# ---XXX

S = "${UNPACKDIR}/${BPN}"
B = "${WORKDIR}/build"

DEEPSTREAM_BASEDIR = "/opt/nvidia/deepstream"
DEEPSTREAM_PATH = "${DEEPSTREAM_BASEDIR}/deepstream-9.1"
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
                rm -rf ${S}${DEEPSTREAM_PATH}/sources/apps/sample_apps/deepstream-nmos
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
    rm -f ${S}${DEEPSTREAM_PATH}/sources/sparse4d/libcustom_preprocess_ocr.so
    rm -f ${S}${DEEPSTREAM_PATH}/sources/sparse4d/libnvocdr.so

    # Drop the sparse4d plugin: its only runtime dep (libtorch/libc10 via
    # pytorch) is not packaged for the target. Nothing else links against it.
    rm -f ${S}${DEEPSTREAM_PATH}/lib/libnvdsgst_sparse4d.so
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
    patchelf --replace-needed libcufft.so libcufft.so.12 ${D}${DEEPSTREAM_PATH}/lib/libnvds_nvmultiobjecttracker.so
    patchelf --replace-needed libcublas.so libcublas.so.13 ${D}${DEEPSTREAM_PATH}/lib/libnvds_nvmultiobjecttracker.so
    patchelf --replace-needed libcufft.so libcufft.so.12 ${D}${DEEPSTREAM_PATH}/lib/libnvds_audiotransform.so
    bbnote "Patching libprotobuf NEEDED to $protobuf_soname"
    patchelf --replace-needed libnppial.so libnppial.so.13 ${D}${DEEPSTREAM_PATH}/lib/libnvds_vpicanmatch.so
    patchelf --replace-needed libnppist.so libnppist.so.13 ${D}${DEEPSTREAM_PATH}/lib/libnvds_vpicanmatch.so
    jsoncpp_soname=$(${OBJDUMP} -p ${STAGING_LIBDIR}/libjsoncpp.so | grep SONAME | awk '{print $2}')
    bbnote "Patching libjsoncpp NEEDED to $jsoncpp_soname"
    patchelf --replace-needed libjsoncpp.so.25 $jsoncpp_soname ${D}${DEEPSTREAM_PATH}/lib/libnvds_rest_server.so
    patchelf --replace-needed libjsoncpp.so.25 $jsoncpp_soname ${D}${libdir}/gstreamer-1.0/deepstream/libnvdsgst_nvmultiurisrcbin.so
    # ---XXX
    cd ${D}${DEEPSTREAM_BASEDIR}
    ln -s deepstream-9.1 deepstream
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
FILES:${PN}-staticdev = " \
    ${libdir}/gstreamer-1.0/deepstream/libnvdsgst_multistream_legacy.a \
    ${DEEPSTREAM_PATH}/lib/libnvds_service_maker_utils.a \
"

RDEPENDS:${PN} = "libgstnvcustomhelper yaml-cpp-080 libcurl tegra-libraries-nvml opentelemetry-cpp"
RDEPENDS:${PN}-samples = "${PN} ${PN}-samples-data libgstnvcustomhelper yaml-cpp-080"
RDEPENDS:${PN}-samples-data = "bash"
RDEPENDS:${PN}-sources = "bash ${PN}-samples-data ${PN}"
RRECOMMENDS:${PN}-sources += "${PN}-dev"
RRECOMMENDS:${PN} = "liberation-fonts"
