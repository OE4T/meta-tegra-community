SUMMARY = "Metropolis Microservices for Jetson"
HOMEPAGE = "http://developer.nvidia.com/jetson"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://usr/share/doc/nvidia-jetson-services/copyright;md5=ca07a9c8eb49d896912ac596c7ca3202"

COMPATIBLE_MACHINE = "(tegra)"

inherit l4t_deb_pkgfeed systemd

COMPATIBLE_MACHINE = "(tegra)"

SRC_COMMON_DEBS = "nvidia-jetson-services_${PV}_arm64.deb;subdir=nvidia-jetson-services"
SRC_URI:append = " file://0001-OE-cross-build-fixups.patch"
SRC_URI[sha256sum] = "027f95b0c8f1b0a988d1a12e4f1b2de9645a42a971cfbc6dbe7a53d803e11937"

S = "${WORKDIR}/nvidia-jetson-services"
B = "${S}"

do_install () {
    install -d ${D}${sysconfdir}/logrotate.d
    install -m 0644 ${B}/etc/logrotate.d/jetson-logging ${D}${sysconfdir}/logrotate.d/

    install -d ${D}${sysconfdir}/modules-load.d/
    install -m 0644 ${B}/usr/lib/modules-load.d/jetson-storage.conf ${D}${sysconfdir}/modules-load.d/

    install -d ${D}${systemd_system_unitdir}
    install -m 0644 ${B}/lib/systemd/system/jetson-sys-monitoring.service ${D}${systemd_system_unitdir}/
    install -m 0644 ${B}/lib/systemd/system/jetson-storage.service ${D}${systemd_system_unitdir}/
    install -m 0644 ${B}/lib/systemd/system/jetson-redis.service ${D}${systemd_system_unitdir}/
    install -m 0644 ${B}/lib/systemd/system/jetson-networking.service ${D}${systemd_system_unitdir}/
    install -m 0644 ${B}/lib/systemd/system/jetson-monitoring.service ${D}${systemd_system_unitdir}/
    install -m 0644 ${B}/lib/systemd/system/jetson-iot-gateway.service ${D}${systemd_system_unitdir}/
    install -m 0644 ${B}/lib/systemd/system/jetson-ingress.service ${D}${systemd_system_unitdir}/
    install -m 0644 ${B}/lib/systemd/system/jetson-gpu-monitoring.service ${D}${systemd_system_unitdir}/
    install -m 0644 ${B}/lib/systemd/system/jetson-firewall.service ${D}${systemd_system_unitdir}/
    install -d ${D}/opt/nvidia/jetson-1.0/services/firewall/
    cp -Rf --preserve=mode,timestamps ${B}/opt/nvidia/jetson-1.0/services/firewall/bin ${D}/opt/nvidia/jetson-1.0/services/firewall/
    install -d ${D}/opt/nvidia/jetson-1.0/services/gpumonitoring/
    cp -Rf --preserve=mode,timestamps ${B}/opt/nvidia/jetson-1.0/services/gpumonitoring/* ${D}/opt/nvidia/jetson-1.0/services/gpumonitoring/
    install -d ${D}/opt/nvidia/jetson-1.0/services/ingress
    cp -Rf --preserve=mode,timestamps ${B}/opt/nvidia/jetson-1.0/services/ingress/* ${D}/opt/nvidia/jetson-1.0/services/ingress/
    install -d ${D}/opt/nvidia/jetson-1.0/services/sysmonitoring
    install -m 0644 ${B}/opt/nvidia/jetson-1.0/services/sysmonitoring/compose.yaml ${D}/opt/nvidia/jetson-1.0/services/sysmonitoring/
    install -d ${D}/opt/nvidia/jetson-1.0/services/storage
    cp -Rf --preserve=mode,timestamps ${B}/opt/nvidia/jetson-1.0/services/storage/* ${D}/opt/nvidia/jetson-1.0/services/storage/
    touch ${D}/opt/nvidia/jetson-1.0/services/storage/storage-first-boot
    install -d ${D}/opt/nvidia/jetson-1.0/services/redis
    cp -Rf --preserve=mode,timestamps ${B}/opt/nvidia/jetson-1.0/services/redis/* ${D}/opt/nvidia/jetson-1.0/services/redis/
    install -d ${D}/opt/nvidia/jetson-1.0/services/networking
    cp -Rf --preserve=mode,timestamps ${B}/opt/nvidia/jetson-1.0/services/networking/* ${D}/opt/nvidia/jetson-1.0/services/networking/
    install -d ${D}/opt/nvidia/jetson-1.0/services/monitoring
    cp -Rf --preserve=mode,timestamps ${B}/opt/nvidia/jetson-1.0/services/monitoring/* ${D}/opt/nvidia/jetson-1.0/services/monitoring/
    install -d ${D}/opt/nvidia/jetson-1.0/services/iotgateway
    cp -Rf --preserve=mode,timestamps ${B}/opt/nvidia/jetson-1.0/services/iotgateway/* ${D}/opt/nvidia/jetson-1.0/services/iotgateway/

    install -d ${D}/opt/nvidia/jetson-configs
    install -m 0644 ${B}/opt/nvidia/jetson-1.0/services/iotgateway/config/.envfile ${D}/opt/nvidia/jetson-configs/.envfile
    install -m 0644 ${B}/opt/nvidia/jetson-1.0/services/storage/jetson-storage.conf ${D}/opt/nvidia/jetson-configs/
    install -m 0644 ${B}/opt/nvidia/jetson-1.0/services/storage/storage-quota.json ${D}//opt/nvidia/jetson-configs/

    cd ${D}/opt/nvidia/
    ln -s jetson-1.0 jetson
    cd -
}

FILES:${PN} += " \
    ${sysconfdir}/logrotate.d \
    ${sysconfdir}/modules-load.d \
    /opt/nvidia/jetson \
    /opt/nvidia/jetson-configs \
    /opt/nvidia/jetson-1.0/services/firewall \
    /opt/nvidia/jetson-1.0/services/gpumonitoring \
    /opt/nvidia/jetson-1.0/services/ingress \
    /opt/nvidia/jetson-1.0/services/sysmonitoring \
    /opt/nvidia/jetson-1.0/services/storage \
    /opt/nvidia/jetson-1.0/services/redis \
    /opt/nvidia/jetson-1.0/services/networking \
    /opt/nvidia/jetson-1.0/services/monitoring \
    /opt/nvidia/jetson-1.0/services/iotgateway \
"

SYSTEMD_SERVICE:${PN} = " \
    jetson-sys-monitoring.service \
    jetson-storage.service \
    jetson-redis.service \
    jetson-networking.service \
    jetson-monitoring.service \
    jetson-iot-gateway.service \
    jetson-ingress.service \
    jetson-gpu-monitoring.service \
    jetson-firewall.service \
"
SYSTEMD_AUTO_ENABLE:${PN} = "disable"

RDEPENDS:${PN} += " \
    libnvvpi3 \
    logrotate \
    ufw \
    bash \
    curl \
    lsscsi \
    lvm2 \
    parted \
    nvme-cli \
    jq \
    quota \
    cryptsetup \
    nvidia-docker \
    docker-compose \
    e2fsprogs-tune2fs \
    glibc-utils \
    util-linux-libuuid \
    util-linux-blkid \
    python3-prometheus-client \
    python3-pyhumps \
"

RRECOMMENDS:${PN} = " \
    kernel-module-quota-v2 \
    coreutils \
"
