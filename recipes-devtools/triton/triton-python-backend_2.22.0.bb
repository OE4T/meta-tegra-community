DESCRIPTION = "Triton backend that enables pre-process, post-processing and other logic to be implemented in Python."
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=7889848dc86811b06ded7bfa9ba39e02"

SRC_URI = "\
    git://github.com/triton-inference-server/python_backend.git;protocol=https;branch=r22.05 \
    file://0001-fix-cmake-build.patch \
"

SRCREV = "6133572a4f090721cc52e79595b70f383397b186"

DEPENDS = " \
    zlib \
    libarchive \
    boost \
    dlpack \
    triton-common \
    triton-core \
    triton-backend \
"

RDEPENDS:${PN} = " \
    python3-pybind11 \
    python3-gevent \
    python3-geventhttpclient \
    python3-rapidjson \
"

COMPATIBLE_MACHINE = "(cuda)"

inherit cmake pkgconfig cuda


PACKAGECONFIG ?= "gpu"
PACKAGECONFIG[gpu] = "-DTRITON_ENABLE_GPU=ON,-DTRITON_ENABLE_GPU=OFF"
PACKAGECONFIG[stats] = "-DTRITON_ENABLE_STATS=ON,-DTRITON_ENABLE_STATS=OFF"
PACKAGECONFIG[nvtx] = "-DTRITON_ENABLE_NVTX=ON,-DTRITON_ENABLE_NVTX=OFF"

FILES:${PN} += " \
    ${libdir}/triton_python_backend_utils.py \
"

SOLIBS = ".so"
FILES_SOLIBSDEV = ""
