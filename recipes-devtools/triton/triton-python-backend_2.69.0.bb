DESCRIPTION = "Triton backend that enables pre-process, post-processing and other logic to be implemented in Python."
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=7889848dc86811b06ded7bfa9ba39e02"

SRC_URI = "\
    git://github.com/triton-inference-server/python_backend.git;protocol=https;branch=r26.05 \
    file://0001-fix-cmake-build.patch \
    file://0002-utils-Serialize-UUID-generation.patch \
"

SRCREV = "47f9e030ea1ef6d33f7069ab129747fe756ace70"

DEPENDS = " \
    zlib \
    libarchive \
    boost \
    dlpack \
    python3 \
    python3-pybind11 \
    triton-common \
    triton-core \
    triton-backend \
"

RDEPENDS:${PN} = " \
    python3-pybind11 \
    python3-gevent \
    python3-geventhttpclient \
    python3-numpy \
    python3-rapidjson \
"

COMPATIBLE_MACHINE = "(cuda)"

inherit cmake pkgconfig cuda python3native python3targetconfig

PACKAGECONFIG ?= "gpu stats"
PACKAGECONFIG[gpu] = "-DTRITON_ENABLE_GPU=ON,-DTRITON_ENABLE_GPU=OFF"
PACKAGECONFIG[stats] = "-DTRITON_ENABLE_STATS=ON,-DTRITON_ENABLE_STATS=OFF"
PACKAGECONFIG[nvtx] = "-DTRITON_ENABLE_NVTX=ON,-DTRITON_ENABLE_NVTX=OFF"

FILES:${PN} += "${libdir}/python"

SOLIBS = ".so"
FILES_SOLIBSDEV = ""
