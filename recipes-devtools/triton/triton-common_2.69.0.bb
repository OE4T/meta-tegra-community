DESCRIPTION = "Common source, scripts and utilities shared across all Triton repositories."
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=7889848dc86811b06ded7bfa9ba39e02"
SECTION = "libs"

SRC_URI = "\
    git://github.com/triton-inference-server/common.git;protocol=https;branch=r26.05 \
    file://0001-Build-fixups.patch \
    file://0002-protobuf-Support-cross-compiling-against-protobuf-an.patch \
    file://0003-protobuf-Add-option-to-build-the-generated-code-as-a.patch \
"

SRCREV = "ecb874c3126f9f07810f4900d4e7b088fee5eb8c"

DEPENDS += "googletest"

COMPATIBLE_MACHINE = "(cuda)"

inherit cmake

EXTRA_OECMAKE = '\
    -DTRITON_COMMON_ENABLE_PROTOBUF_PYTHON=OFF \
    -DTRITON_VERSION="${PV}" \
'

PACKAGECONFIG ??= "grpc protobuf rapidjson"
PACKAGECONFIG[grpc] = "-DTRITON_COMMON_ENABLE_GRPC=ON -DGRPC_CPP_PLUGIN_EXECUTABLE=${STAGING_BINDIR_NATIVE}/grpc_cpp_plugin,-DTRITON_COMMON_ENABLE_GRPC=OFF,grpc grpc-native"
PACKAGECONFIG[protobuf] = "-DTRITON_COMMON_ENABLE_PROTOBUF=ON -DTRITON_COMMON_ENABLE_SHARED_PROTO=ON -DProtobuf_PROTOC_EXECUTABLE=${STAGING_BINDIR_NATIVE}/protoc,-DTRITON_COMMON_ENABLE_PROTOBUF=OFF,protobuf protobuf-native"
PACKAGECONFIG[rapidjson] = "-DTRITON_COMMON_ENABLE_JSON=ON,-DTRITON_COMMON_ENABLE_JSON=OFF,rapidjson"
