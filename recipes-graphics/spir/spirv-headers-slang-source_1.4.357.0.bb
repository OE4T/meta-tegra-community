SUMMARY = "SPIR-V headers sources staged for Slang"
DESCRIPTION = "SPIR-V headers source tree staged for source-level inclusion by Slang. \
Slang needs SPIR-V tokens newer than the spirv-headers release in openembedded-core, \
which stays pinned in lockstep with the rest of the Vulkan SDK recipes."
HOMEPAGE = "https://www.khronos.org/registry/spir-v"
SECTION = "graphics"
LICENSE = "MIT & CC-BY-4.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=a0dcaa512cc2dee95fe0fd791ee83a18"

# Matches the external/spirv-headers submodule of shader-slang v2026.14.
SRC_URI = "git://github.com/KhronosGroup/SPIRV-Headers;protocol=https;branch=main;tag=vulkan-sdk-${PV}"
SRCREV = "29981f65241605e08b0ede4cfeb999fe3b723c6a"

do_configure[noexec] = "1"
do_compile[noexec] = "1"

# Slang add_subdirectory()s <path>/spirv-headers, so stage the CMake project
# itself rather than an installed tree.
do_install() {
    install -d ${D}${datadir}/spirv-headers-slang-source/spirv-headers
    cp -R --no-dereference --preserve=mode,links \
        ${S}/CMakeLists.txt ${S}/LICENSE ${S}/cmake ${S}/include \
        ${D}${datadir}/spirv-headers-slang-source/spirv-headers/
}

FILES:${PN} += "${datadir}/spirv-headers-slang-source"

BBCLASSEXTEND = "native nativesdk"
