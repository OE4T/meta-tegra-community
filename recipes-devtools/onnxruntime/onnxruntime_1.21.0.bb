SUMMARY = "ONNX Runtime: cross-platform, high performance ML inferencing and training accelerator"
HOMEPAGE = "https://github.com/microsoft/onnxruntime"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=0f7e3b1308cb5c00b372a6e78835732d"

SRC_URI = " \
    git://github.com/microsoft/onnxruntime.git;protocol=https;nobranch=1;tag=v${PV} \
    git://gitlab.com/libeigen/eigen.git;protocol=https;nobranch=1;name=libeigen;destsuffix=${BPN}-${PV}/_deps/eigen-src \
    git://github.com/NVIDIA/cudnn-frontend.git;protocol=https;nobranch=1;name=cudnn_frontend;destsuffix=${BPN}-${PV}/_deps/cudnn_frontend-src \
"
SRCREV = "e0b66cad282043d4377cea5269083f17771b6dfc"
SRCREV_libeigen = "1d8b82b0740839c0de7f1242a3585e3390ff5f33"
SRCREV_cudnn_frontend = "de355c7094af70467f2b264f531ab5c5f4401c42"

SRCREV_FORMAT = "onnxruntime_libeigen_cudnn_frontend"

SRC_URI += " \
    file://0001-Use-external-library-dependencies.patch \
"

COMPATIBLE_MACHINE = "(cuda)"

inherit cmake cuda

B = "${S}"

DEPENDS += " \
    boost \
    abseil-cpp \
    cudnn \
    date \
    flatbuffers \
    flatbuffers-native \
    nlohmann-json \
    nsync \
    onnx \
    microsoft-gsl \
    protobuf \
    protobuf-native \
    re2 \
    safeint \
    tensorrt-plugins \
    cutlass \
    dlpack \
"

RDEPENDS:${PN} += " \
    cuda-cudart \
    cuda-nvrtc \
"

EXTRA_OECMAKE = " \
    -DONNX_CUSTOM_PROTOC_EXECUTABLE=${STAGING_BINDIR_NATIVE}/protoc \
    -Donnxruntime_BUILD_SHARED_LIB=ON \
    -Donnxruntime_BUILD_UNIT_TESTS=OFF \
    -Donnxruntime_USE_TENSORRT=ON \
    -Donnxruntime_USE_TENSORRT_BUILTIN_PARSER=TRUE \
    -Donnxruntime_CROSS_COMPILING=ON \
    -Donnxruntime_USE_CUDA=ON \
    -Donnxruntime_CUDA_VERSION=${CUDA_VERSION} \
    -Donnxruntime_CUDA_HOME=${CUDA_TOOLKIT_ROOT} \
    -Donnxruntime_CUDNN_HOME=${CUDA_TOOLKIT_ROOT} \
    -Donnxruntime_ENABLE_CPUINFO=OFF \
    -DFETCHCONTENT_TRY_FIND_PACKAGE_MODE=ALWAYS \
    -Donnxruntime_USE_PREINSTALLED_EIGEN=OFF \
    -DCMAKE_SKIP_RPATH=ON \
    -DCMAKE_CXX_STANDARD=17 \
"

OECMAKE_CXX_FLAGS:append = " -Wno-array-bounds -Wno-deprecated-declarations -Wno-unused-variable -Wno-template-id-cdtor -Wno-range-loop-construct -Wno-maybe-uninitialized -Wno-error=cpp"
OECMAKE_SOURCEPATH = "${S}/cmake"

SOLIBS = ".so"
FILES_SOLIBSDEV = ""
FILES:${PN} += "${libdir}/libonnxruntime.so* ${libdir}/libonnxruntime_providers*so"

INSANE_SKIP:${PN} = "buildpaths dev-so"
INSANE_SKIP:${PN}-dev = "dev-elf buildpaths"
INSANE_SKIP:${PN}-dbg = "buildpaths"
