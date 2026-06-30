SUMMARY = "nvCOMP library provides fast lossless data compression and decompression using a GPU"
HOMEPAGE = "https://docs.nvidia.com/cuda/nvcomp"
LICENSE = "Proprietary"
LIC_FILES_CHKSUM = "file://nvcomp-linux-sbsa-${PV}_cuda13-archive/LICENSE;md5=85649c377f72fda614b170fb9e1216f3"

COMPATIBLE_MACHINE = "(cuda)"

SRC_URI = "https://developer.download.nvidia.com/compute/nvcomp/redist/nvcomp/linux-sbsa/nvcomp-linux-sbsa-${PV}_cuda13-archive.tar.xz;subdir=${BP}"
SRC_URI[sha256sum] = "c7a7eaabb03ac9893144787cdb41c9c838d9af76124e2f3246f7e20ebbd8b9cc"

do_install () {
    install -d ${D}${bindir}
    install -m 0755 ${S}/nvcomp-linux-sbsa-${PV}_cuda13-archive/bin/nvlzcat ${D}${bindir}/
    install -d ${D}${includedir}
    cp -rd --no-preserve=ownership ${S}/nvcomp-linux-sbsa-${PV}_cuda13-archive/include/* ${D}${includedir}/
    install -d ${D}${libdir}
    cp -rd --no-preserve=ownership ${S}/nvcomp-linux-sbsa-${PV}_cuda13-archive/lib/cmake ${D}${libdir}/
    install -m 0644 ${S}/nvcomp-linux-sbsa-${PV}_cuda13-archive/lib/libnvcomp_cpu.so.5.2.0.10 ${D}${libdir}/
    install -m 0644 ${S}/nvcomp-linux-sbsa-${PV}_cuda13-archive/lib/libnvcomp_cpu_static.a ${D}${libdir}/
    install -m 0644 ${S}/nvcomp-linux-sbsa-${PV}_cuda13-archive/lib/libnvcomp.so.5.2.0.10 ${D}${libdir}/
    install -m 0644 ${S}/nvcomp-linux-sbsa-${PV}_cuda13-archive/lib/libnvcomp_static.a ${D}${libdir}/
    ln -s libnvcomp_cpu.so.5 ${D}${libdir}/libnvcomp_cpu.so
    ln -s libnvcomp.so.5.2.0.10 ${D}${libdir}/libnvcomp_cpu.so.5
    ln -s libnvcomp.so.5 ${D}${libdir}/libnvcomp.so
    ln -s libnvcomp_cpu.so.5.2.0.10 ${D}${libdir}/libnvcomp.so.5
}

SYSROOT_DIRS:append = " ${bindir}"

INSANE_SKIP:${PN} = "already-stripped"
