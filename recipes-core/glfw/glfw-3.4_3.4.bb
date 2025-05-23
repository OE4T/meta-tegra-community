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

SRC_URI = "git://github.com/glfw/glfw.git;protocol=https;branch=master;tag=${PV}"
SRCREV = "7b6aead9fb88b3623e3b3725ebb42670cbe4c579"

EXTRA_OECMAKE += "-DBUILD_SHARED_LIBS=ON -DGLFW_BUILD_DOCS=OFF"

CFLAGS += "-fPIC"

DEPENDS = "libpng zlib"
REQUIRED_DISTRO_FEATURES = "opengl x11"
ANY_OF_DISTRO_FEATURES = "wayland x11"

PACKAGECONFIG ??= "x11 wayland"

PACKAGECONFIG[wayland] = "-DGLFW_BUILD_WAYLAND=ON,-DGLFW_BUILD_WAYLAND=OFF,wayland wayland-native wayland-protocols libxkbcommon"
PACKAGECONFIG[x11] = ",,libxrandr libxinerama libxi libxcursor libglu"

COMPATIBLE_HOST:libc-musl = "null"
