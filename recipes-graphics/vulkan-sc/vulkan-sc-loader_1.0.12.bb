SUMMARY = "Vulkan SC loader for drivers and layers"
HOMEPAGE = "https://github.com/KhronosGroup/VulkanSC-Loader"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE.txt;md5=7dbefed23242760aa3475ee42801c5ac"

SRC_URI = "git://github.com/KhronosGroup/VulkanSC-Loader.git;nobranch=1;protocol=https;tag=vksc${PV}"
SRCREV = "6f097bd30f79bc1bb61d091a70d8c9a11b253211"

COMPATIBLE_MACHINE = "(tegra)"
REQUIRED_DISTRO_FEATURES = "vulkan"

inherit cmake features_check

DEPENDS = "vulkan-sc-headers tegra-libraries-nvsci python3-native"

EXTRA_OECMAKE = "\
    -DVULKANSC=ON \
    -DUPDATE_DEPS=OFF \
    -DBUILD_TESTS=OFF \
    -DBUILD_WSI_XCB_SUPPORT=OFF \
    -DBUILD_WSI_XLIB_SUPPORT=OFF \
    -DBUILD_WSI_WAYLAND_SUPPORT=OFF \
    -DBUILD_WSI_SCI_SUPPORT=ON \
    -DVulkanHeaders_INCLUDE_DIR=${STAGING_INCDIR}/VulkanSC \
    -DVulkanRegistry_DIR=${STAGING_DATADIR}/vulkan-sc/registry \
    -DSYSCONFDIR=${sysconfdir} \
    "
