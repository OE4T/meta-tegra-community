SUMMARY = "Easy to integrate Vulkan memory allocation library"
HOMEPAGE = "https://github.com/GPUOpen-LibrariesAndSDKs/VulkanMemoryAllocator"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE.txt;md5=038b37427bb6a76e236931139bcd777d"

SRC_URI = "git://github.com/GPUOpen-LibrariesAndSDKs/VulkanMemoryAllocator.git;protocol=https;nobranch=1;tag=v${PV}"
SRCREV = "3aa921224c154a0d2c43912bc88e1c42ce1f7607"

REQUIRED_DISTRO_FEATURES = "vulkan"

inherit cmake features_check

# Header-only: everything (headers, the CMake package under
# ${datadir}/cmake) lands in -dev by default, leaving the main
# package empty.
ALLOW_EMPTY:${PN} = "1"
