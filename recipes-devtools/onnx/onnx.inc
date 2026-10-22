SUMMARY = "Open standard for machine learning interoperability"
HOMEPAGE = "https://github.com/onnx/onnx"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=3b83ef96387f14655fc854ddc3c6bd57"

SRC_URI = " \
    git://github.com/onnx/onnx.git;protocol=https;branch=rel-${PV};tag=v${PV} \
    file://0001-Updates-for-OE-cross-builds.patch \
    file://0002-Find-Python-with-Development.Module-for-nanobind-in-.patch \
    file://0003-setup.py-support-OE-cross-builds-interpreter-sysroot.patch \
    file://0004-Serialize-gen_proto-invocations-to-avoid-parallel-ra.patch \
    file://0005-Avoid-embedding-proto-sources-in-libonnx-prevents-du.patch \
"
SRCREV = "d3f6b795aedb48eaecc881bf5e8f5dd6efbe25b3"

DEPENDS += " \
    protobuf \
    protobuf-native \
"
