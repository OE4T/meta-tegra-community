SUMMARY = "Vulkan tools for Vulkan SC development"
HOMEPAGE = "https://github.com/KhronosGroup/VulkanTools-ForSC"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE.txt;md5=01b472509d76a2a90b27cbbf34853a05"

SRC_URI = "git://github.com/KhronosGroup/VulkanTools-ForSC.git;branch=sc_main;protocol=https;tag=nv-vulkansc-sdk-${PV} \
           file://0001-limit-optional-deps-and-use-shared-crypto.patch \
           file://0002-recognize-vulkan-platform-guards.patch \
           "
SRCREV = "449dbde8eb021662b91b36a4eea166517673c0cb"

COMPATIBLE_MACHINE = "(tegra)"
REQUIRED_DISTRO_FEATURES = "vulkan"

inherit cmake features_check pkgconfig python3native

DEPENDS = "openssl python3-pyparsing-native vulkan-headers vulkan-loader vulkan-utility-libraries valijson jsoncpp"

EXTRA_OECMAKE = "\
    -DBUILD_APIDUMP=OFF \
    -DBUILD_VKJSON=ON \
    -DBUILD_MONITOR=ON \
    -DBUILD_SCREENSHOT=ON \
    -DBUILD_VIA=ON \
    -DBUILD_JSON_GEN_LAYER_TESTS=OFF \
    -DBUILD_LAYERMGR=OFF \
    -DBUILD_TESTS=OFF \
    -DBUILD_WERROR=OFF \
    -DBUILD_WSI_XCB_SUPPORT=OFF \
    -DBUILD_WSI_XLIB_SUPPORT=OFF \
    -DBUILD_WSI_WAYLAND_SUPPORT=OFF \
    -DUPDATE_DEPS=OFF \
    -DVULKAN_HEADERS_INSTALL_DIR=${STAGING_EXECPREFIXDIR} \
    "

SOLIBS = ".so"
FILES_SOLIBSDEV = ""

PACKAGES =+ "\
    ${PN}-json-gen \
    ${PN}-monitor \
    ${PN}-screenshot \
    ${PN}-via \
    "

FILES:${PN}-json-gen = "${libdir}/libVkLayer_json_gen.so ${sysconfdir}/vulkan/explicit_layer.d/VkLayer_json_gen.json"
FILES:${PN}-monitor = "${libdir}/libVkLayer_monitor.so ${sysconfdir}/vulkan/explicit_layer.d/VkLayer_monitor.json"
FILES:${PN}-screenshot = "${libdir}/libVkLayer_screenshot.so ${sysconfdir}/vulkan/explicit_layer.d/VkLayer_screenshot.json"
FILES:${PN}-via = "${bindir}/vkvia"

RDEPENDS:${PN}-json-gen = "vulkan-loader"
