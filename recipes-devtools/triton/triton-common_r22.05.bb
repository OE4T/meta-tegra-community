DESCRIPTION = "Common source, scripts and utilities shared across all Triton repositories."
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=7889848dc86811b06ded7bfa9ba39e02"
SECTION = "libs"

SRC_URI = "\
    git://github.com/triton-inference-server/common.git;protocol=https;branch=${PV} \
    file://0001-fix-cmake-build.patch \
"

SRCREV = "feca9eadf25eb0230eaac0e9f2ce8f6447a0e9f1"

S = "${WORKDIR}/git"

inherit cmake

PACKAGECONFIG ??= "protobuf rapidjson"
PACKAGECONFIG[grpc] = "-DTRITON_COMMON_ENABLE_GRPC=ON,-DTRITON_COMMON_ENABLE_GRPC=OFF,grpc"
PACKAGECONFIG[protobuf] = "-DTRITON_COMMON_ENABLE_PROTOBUF=ON,-DTRITON_COMMON_ENABLE_PROTOBUF=OFF,protobuf protobuf-native"
PACKAGECONFIG[rapidjson] = "-DTRITON_COMMON_ENABLE_JSON=ON,-DTRITON_COMMON_ENABLE_JSON=OFF,rapidjson"
