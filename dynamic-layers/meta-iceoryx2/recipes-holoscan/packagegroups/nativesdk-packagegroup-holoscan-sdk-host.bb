SUMMARY = "Host packages for building Holoscan SDK applications with an SDK"
DESCRIPTION = "Build-host tools the Holoscan SDK CMake package runs: nvcc, \
flatc, slangc and Python."

PACKAGE_ARCH = "${TUNE_PKGARCH}"

inherit packagegroup
inherit_defer nativesdk

PACKAGEGROUP_DISABLE_COMPLEMENTARY = "1"

RDEPENDS:${PN} = " \
    nativesdk-packagegroup-cuda-sdk-host \
    nativesdk-cuda-cudart-dev \
    nativesdk-cuda-crt-dev \
    nativesdk-cuda-nvcc-headers \
    nativesdk-flatbuffers-compiler \
    nativesdk-shader-slang \
    nativesdk-python3-core \
    nativesdk-python3-crypt \
    nativesdk-python3-io \
    nativesdk-python3-json \
    nativesdk-python3-shell \
"
