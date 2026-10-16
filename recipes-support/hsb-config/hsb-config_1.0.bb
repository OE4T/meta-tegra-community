SUMMARY = "Holoscan Sensor Bridge host setup on mgbe0_0: static address and PTP"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = " \
    file://60-hsb-mgbe0_0.network \
    file://hsb-ptp.conf \
    file://phc2sys-mgbe0_0.service \
    file://ptp4l-mgbe0_0.service \
"

S = "${UNPACKDIR}"

inherit allarch systemd features_check

REQUIRED_DISTRO_FEATURES = "systemd"

do_install() {
    install -d ${D}${sysconfdir}/linuxptp
    install -m 0644 ${UNPACKDIR}/hsb-ptp.conf ${D}${sysconfdir}/linuxptp/

    install -d ${D}${systemd_system_unitdir}
    install -m 0644 ${UNPACKDIR}/phc2sys-mgbe0_0.service ${D}${systemd_system_unitdir}/
    install -m 0644 ${UNPACKDIR}/ptp4l-mgbe0_0.service ${D}${systemd_system_unitdir}/

    install -d ${D}${systemd_unitdir}/network
    install -m 0644 ${UNPACKDIR}/60-hsb-mgbe0_0.network ${D}${systemd_unitdir}/network/
}

FILES:${PN} += "${systemd_unitdir}/network"

SYSTEMD_SERVICE:${PN} = "phc2sys-mgbe0_0.service ptp4l-mgbe0_0.service"

RDEPENDS:${PN} = "linuxptp"
