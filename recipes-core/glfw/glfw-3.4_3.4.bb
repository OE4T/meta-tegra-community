SUMMARY  = "A multi-platform library for OpenGL, OpenGL ES, Vulkan, window and input"
HOMEPAGE = "https://www.glfw.org/"
DESCRIPTION = "GLFW is an Open Source, multi-platform library for OpenGL, \
OpenGL ES and Vulkan application development. It provides a simple, \
platform-independent API for creating windows, contexts and surfaces, reading \
input, handling events, etc."
LICENSE  = "Zlib"
LIC_FILES_CHKSUM = "file://LICENSE.md;md5=98d93d1ddc537f9b9ea6def64e046b5f"
SECTION = "lib"

inherit pkgconfig cmake features_check

SRC_URI = "git://github.com/glfw/glfw.git;protocol=https;nobranch=1;tag=${PV}"
SRCREV = "7b6aead9fb88b3623e3b3725ebb42670cbe4c579"

REQUIRED_DISTRO_FEATURES = "opengl"
ANY_OF_DISTRO_FEATURES = "wayland x11"

PACKAGECONFIG ??= "${@bb.utils.filter('DISTRO_FEATURES', 'wayland x11', d)}"

# GLFW dlopen()s its platform libraries, so no NEEDED entry pulls them in.
PACKAGECONFIG[wayland] = "-DGLFW_BUILD_WAYLAND=ON,-DGLFW_BUILD_WAYLAND=OFF,wayland wayland-native libxkbcommon,wayland libxkbcommon"
PACKAGECONFIG[x11] = "-DGLFW_BUILD_X11=ON,-DGLFW_BUILD_X11=OFF,libx11 libxrandr libxinerama libxi libxcursor libxext,libx11 libxrandr libxinerama libxi libxcursor libxext libxrender"

EXTRA_OECMAKE += " \
    -DBUILD_SHARED_LIBS=ON \
    -DGLFW_BUILD_DOCS=OFF \
    -DGLFW_BUILD_EXAMPLES=OFF \
    -DGLFW_BUILD_TESTS=OFF \
"

RCONFLICTS:${PN} = "glfw"
RCONFLICTS:${PN}-dev = "glfw-dev"

COMPATIBLE_HOST:libc-musl = "null"

CVE_PRODUCT = "glfw"
