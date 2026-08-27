SUMMARY = "Compile-time reflection and serialization for C++20"
DESCRIPTION = "reflect-cpp provides struct reflection and format-agnostic serialization for \
C++20, deriving field names and types from the struct definition rather than from macros or \
generated code."
HOMEPAGE = "https://github.com/getml/reflect-cpp"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=34cfa8d5bb7702c509b244b91ac4d987"

SRC_URI = "\
    git://github.com/getml/reflect-cpp.git;protocol=https;nobranch=1;tag=v${PV} \
    file://0001-Install-version-file-under-CMake-expected-name.patch \
"
SRCREV = "1327ef1d0b403f1424776da4c47bf0c7a6b0feaa"

inherit cmake

EXTRA_OECMAKE = " \
    -DREFLECTCPP_BUILD_SHARED=ON \
    -DREFLECTCPP_JSON=ON \
    -DREFLECTCPP_ALL_FORMATS=OFF \
    -DREFLECTCPP_USE_BUNDLED_DEPENDENCIES=ON \
    -DREFLECTCPP_USE_VCPKG=OFF \
    -DREFLECTCPP_BUILD_TESTS=OFF \
    -DREFLECTCPP_BUILD_BENCHMARKS=OFF \
    -DREFLECTCPP_CHECK_HEADERS=OFF \
    -DREFLECTCPP_INSTALL=ON \
    -DINCLUDE_INSTALL_DIR=include \
"
