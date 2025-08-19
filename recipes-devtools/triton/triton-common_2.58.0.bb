DESCRIPTION = "Common source, scripts and utilities shared across all Triton repositories."
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=7889848dc86811b06ded7bfa9ba39e02"
SECTION = "libs"

SRC_URI = "\
    git://github.com/triton-inference-server/common.git;protocol=https;branch=r25.05 \
    file://0001-Build-fixups.patch \
    file://0002-cmake-Set-minimum-required-version-to-3.5-for-CMake-.patch \
"

SRCREV = "e4e00edd25af07d2f0e81e2025dbd40daa1dffc2"

DEPENDS += "googletest"

COMPATIBLE_MACHINE = "(cuda)"

inherit cmake

PACKAGECONFIG ??= "protobuf rapidjson"
PACKAGECONFIG[grpc] = "-DTRITON_COMMON_ENABLE_GRPC=ON,-DTRITON_COMMON_ENABLE_GRPC=OFF,grpc"
PACKAGECONFIG[protobuf] = "-DTRITON_COMMON_ENABLE_PROTOBUF=ON,-DTRITON_COMMON_ENABLE_PROTOBUF=OFF,protobuf protobuf-native"
PACKAGECONFIG[rapidjson] = "-DTRITON_COMMON_ENABLE_JSON=ON,-DTRITON_COMMON_ENABLE_JSON=OFF,rapidjson"
