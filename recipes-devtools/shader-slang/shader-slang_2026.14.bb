SUMMARY = "Slang shading language compiler"
DESCRIPTION = "Slang shading language compiler, a real-time shading language for GPU programming"
HOMEPAGE = "https://github.com/shader-slang/slang"
LICENSE = "Apache-2.0-with-LLVM-exception"
LIC_FILES_CHKSUM = "file://LICENSE;md5=67d534b191503811a1a98f155e4b698c"

SRC_URI = " \
    git://github.com/shader-slang/slang.git;protocol=https;branch=master;tag=v${PV} \
    file://0001-cmake-Add-system-library-options-for-cmark-gfm-and-fa.patch \
    file://0002-cmake-Do-not-require-glslang-s-CMake-package-for-the-.patch \
"
SRCREV = "4265906358862245cb25f091cb47dc426c15267b"

inherit cmake pkgconfig python3native

DEPENDS = " \
    unordered-dense \
    miniz \
    lz4 \
    fastfloat \
    cmark-gfm \
    spirv-headers \
    vulkan-headers \
    glslang \
    spirv-tools \
"
DEPENDS:append:class-target = " shader-slang-native"
DEPENDS:append:class-nativesdk = " shader-slang-native"
DEPENDS:append:class-native = " lua-slang-source-native"

EXTRA_OECMAKE = " \
    -DCMAKE_BUILD_TYPE=Release \
    -DSLANG_VERSION_NUMERIC=${PV} \
    -DSLANG_VERSION_FULL=${PV} \
    -DSLANG_LIB_TYPE=SHARED \
    -DSLANG_SLANG_LLVM_FLAVOR=DISABLE \
    -DSLANG_ENABLE_DXIL=OFF \
    -DSLANG_ENABLE_GFX=OFF \
    -DSLANG_ENABLE_SLANG_RHI=OFF \
    -DSLANG_ENABLE_TESTS=OFF \
    -DSLANG_ENABLE_EXAMPLES=OFF \
    -DSLANG_ENABLE_REPLAYER=OFF \
    -DSLANG_ENABLE_CUDA=OFF \
    -DSLANG_ENABLE_OPTIX=OFF \
    -DSLANG_ENABLE_NVAPI=OFF \
    -DSLANG_ENABLE_AFTERMATH=OFF \
    -DSLANG_ENABLE_XLIB=OFF \
    -DSLANG_ENABLE_PCH=OFF \
    -DSLANG_EXCLUDE_DAWN=ON \
    -DSLANG_EXCLUDE_TINT=ON \
    -DSLANG_STANDARD_MODULE_DEVELOP_BUILD=OFF \
    -DSLANG_USE_SYSTEM_MINIZ=ON \
    -DSLANG_USE_SYSTEM_LZ4=ON \
    -DSLANG_USE_SYSTEM_UNORDERED_DENSE=ON \
    -DSLANG_USE_SYSTEM_VULKAN_HEADERS=ON \
    -DSLANG_USE_SYSTEM_CMARK=ON \
    -DSLANG_USE_SYSTEM_FAST_FLOAT=ON \
    -DSLANG_USE_SYSTEM_SPIRV_HEADERS=ON \
    -DSLANG_ENABLE_SLANG_GLSLANG=ON \
    -DSLANG_USE_SYSTEM_GLSLANG=ON \
    -DSLANG_USE_SYSTEM_SPIRV_TOOLS=ON \
    -DFETCHCONTENT_FULLY_DISCONNECTED=ON \
"

EXTRA_OECMAKE:append:class-native = " \
    -DSLANG_ENABLE_SLANGD=OFF \
    -DSLANG_OVERRIDE_LUA_PATH=${STAGING_DATADIR_NATIVE}/slang-lua-source \
    -Dcmark-gfm_DIR=${STAGING_LIBDIR_NATIVE}/cmake \
"

EXTRA_OECMAKE:append:class-target = " \
    -DSLANG_ENABLE_SPLIT_DEBUG_INFO=OFF \
    -DSLANG_GENERATORS_PATH=${STAGING_DIR_NATIVE}${libexecdir}/slang-generators \
    -Dcmark-gfm_DIR=${STAGING_LIBDIR}/cmake \
"

EXTRA_OECMAKE:append:class-nativesdk = " \
    -DSLANG_ENABLE_SPLIT_DEBUG_INFO=OFF \
    -DSLANG_GENERATORS_PATH=${STAGING_LIBEXECDIR_NATIVE}/slang-generators \
    -Dcmark-gfm_DIR=${STAGING_LIBDIR}/cmake \
"

OECMAKE_TARGET_COMPILE:class-native = "all-generators slangc slang-glslang slang-glsl-module"

do_install:class-native() {
    install -d ${D}${libexecdir}/slang-generators
    for f in ${B}/generators/Release/bin/* ${B}/generators/Release/lib/*.so; do
        case "$f" in
            *.dwarf) continue ;;
        esac
        install -m 0755 "$f" ${D}${libexecdir}/slang-generators/
    done

    install -d ${D}${bindir} ${D}${libdir}
    install -m 0755 ${B}/Release/bin/slangc ${D}${bindir}/
    install -m 0755 ${B}/Release/lib/libslang-compiler.so.0.${PV} ${D}${libdir}/
    install -m 0755 ${B}/Release/lib/libslang-glslang-${PV}.so ${D}${libdir}/
    install -m 0755 ${B}/Release/lib/libslang-glsl-module-${PV}.so ${D}${libdir}/
}

# dlopen()ed by name at runtime, so not -dev material.
PACKAGES =+ "${PN}-modules"
FILES:${PN}-modules = " \
    ${libdir}/libslang-glslang-*.so \
    ${libdir}/libslang-glsl-module-*.so \
"
INSANE_SKIP:${PN}-modules += "dev-so"
RDEPENDS:${PN} += "${PN}-modules"

FILES:${PN} += "${libdir}/slang-standard-module-${PV}"

# find_package(slang) fails unless every imported executable exists.
SYSROOT_DIRS:append:class-target = " ${bindir}"

BBCLASSEXTEND = "native nativesdk"
