SUMMARY = "Vulkan SC parameter validation layer"
HOMEPAGE = "https://github.com/KhronosGroup/VulkanSC-ValidationLayers"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE.txt;md5=8df9e8826734226d08cb412babfa599c"

SRC_URI = "git://github.com/KhronosGroup/VulkanSC-ValidationLayers.git;nobranch=1;protocol=https;tag=vksc${PV}"
SRCREV = "5ecd91d5684ee2fc5219965cc0b20f3c0bb1ab83"

COMPATIBLE_MACHINE = "(tegra)"
REQUIRED_DISTRO_FEATURES = "vulkan"

inherit cmake features_check pkgconfig

DEPENDS = "vulkan-sc-headers tegra-libraries-nvsci spirv-headers spirv-tools python3-native"

EXTRA_OECMAKE = "\
    -DVULKANSC=ON \
    -DUPDATE_DEPS=OFF \
    -DBUILD_TESTS=OFF \
    -DBUILD_LAYERS=ON \
    -DUSE_ROBIN_HOOD_HASHING=OFF \
    -DBUILD_WSI_XCB_SUPPORT=OFF \
    -DBUILD_WSI_XLIB_SUPPORT=OFF \
    -DBUILD_WSI_WAYLAND_SUPPORT=OFF \
    -DBUILD_WSI_SCI_SUPPORT=ON \
    -DVulkanHeaders_INCLUDE_DIR=${STAGING_INCDIR}/VulkanSC \
    -DVulkanRegistry_DIR=${STAGING_DATADIR}/vulkan-sc/registry \
    -DSPIRV_HEADERS_INSTALL_DIR=${STAGING_DIR_TARGET}${exec_prefix} \
    -DSPIRV_TOOLS_INSTALL_DIR=${STAGING_DIR_TARGET}${exec_prefix} \
    "

do_install:append() {
    install -d ${D}${sysconfdir}/vulkansc/explicit_layer.d
    install -m 0644 ${B}/layers/staging-json/VkSCLayer_khronos_validation.json \
        ${D}${sysconfdir}/vulkansc/explicit_layer.d/
    rm -f ${D}${datadir}/vulkan/explicit_layer.d/VkSCLayer_khronos_validation.json
    rmdir ${D}${datadir}/vulkan/explicit_layer.d ${D}${datadir}/vulkan ${D}${datadir}
}

SOLIBS = ".so"
FILES_SOLIBSDEV = ""
FILES:${PN} += "${sysconfdir}/vulkansc/explicit_layer.d/VkSCLayer_khronos_validation.json"
