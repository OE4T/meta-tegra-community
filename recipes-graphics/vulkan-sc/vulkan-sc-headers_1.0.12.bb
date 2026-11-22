SUMMARY = "Vulkan SC API headers and registry"
HOMEPAGE = "https://github.com/KhronosGroup/VulkanSC-Headers"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE.txt;md5=3b83ef96387f14655fc854ddc3c6bd57"

SRC_URI = "git://github.com/KhronosGroup/VulkanSC-Headers.git;branch=sc_main;protocol=https;tag=vksc${PV}"
SRCREV = "dd9e112afaf5b4c0496a6f0ce8024fd78d41fe10"

COMPATIBLE_MACHINE = "(tegra)"
REQUIRED_DISTRO_FEATURES = "vulkan"

inherit features_check

do_configure[noexec] = "1"
do_compile[noexec] = "1"

do_install() {
    install -d ${D}${includedir}/VulkanSC/vulkan
    install -m 0644 ${S}/include/vulkan/* ${D}${includedir}/VulkanSC/vulkan/

    install -d ${D}${datadir}/vulkan-sc/registry
    install -m 0644 ${S}/registry/vk.xml ${D}${datadir}/vulkan-sc/registry/
}

FILES:${PN}-dev = "${includedir}/VulkanSC/ ${datadir}/vulkan-sc/registry/"
ALLOW_EMPTY:${PN} = "1"
