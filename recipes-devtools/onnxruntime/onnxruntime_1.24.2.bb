SUMMARY = "ONNX Runtime: cross-platform, high performance ML inferencing and training accelerator"
HOMEPAGE = "https://github.com/microsoft/onnxruntime"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=0f7e3b1308cb5c00b372a6e78835732d"

SRC_URI = " \
    git://github.com/microsoft/onnxruntime.git;protocol=https;nobranch=1;tag=v${PV} \
    git://github.com/NVIDIA/cudnn-frontend.git;protocol=https;nobranch=1;name=cudnn_frontend;destsuffix=${BPN}-${PV}/_deps/cudnn_frontend-src \
"
SRCREV = "058787ceead760166e3c50a0a4cba8a833a6f53f"
SRCREV_cudnn_frontend = "666996fe3960f27170d1527e5579ba24c8d3380a"

SRCREV_FORMAT = "onnxruntime_cudnn_frontend"

SRC_URI += " \
    file://0001-Use-external-library-dependencies.patch \
    file://0002-Skip-redundant-ONNX-opset-registration-when-static-r.patch \
"

COMPATIBLE_MACHINE = "(cuda)"

inherit cmake cuda setuptools3

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
    onnx-ort \
    microsoft-gsl \
    protobuf \
    protobuf-native \
    re2 \
    safeint \
    tensorrt-plugins \
    cutlass \
    dlpack \
    libeigen \
    python3-pybind11 \
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
    ${@bb.utils.contains('PACKAGECONFIG', 'python', '-Donnxruntime_ENABLE_PYTHON=ON', '', d)} \
    -DCMAKE_CUDA_COMPILER_FORCED=TRUE \
"

# onnx-ort installs its private, static, registration-disabled ONNX under
# ${prefix}/onnx-ort. Add that prefix to CMAKE_FIND_ROOT_PATH so ORT's
# find_package(ONNX) (FETCHCONTENT_TRY_FIND_PACKAGE_MODE=ALWAYS) resolves to it
# instead of building ONNX from source or finding the shared system onnx.
OECMAKE_EXTRA_ROOT_PATH = "${RECIPE_SYSROOT}${prefix}/onnx-ort"

OECMAKE_CXX_FLAGS:append = " -Wno-array-bounds -Wno-deprecated-declarations -Wno-unused-variable -Wno-template-id-cdtor -Wno-range-loop-construct -Wno-maybe-uninitialized -Wno-error=cpp -Wno-error=uninitialized -Wno-error=unused-but-set-parameter"
# -Wsfinae-incomplete was introduced in GCC 16. On older GCC the -Wno-error=
# form is rejected outright ("cc1plus: error: no option '-Wsfinae-incomplete'"),
# which breaks even the CMake compiler ABI test in do_configure. Only pass it
# where the compiler actually knows the option.
OECMAKE_CXX_FLAGS:append = "${@' -Wno-error=sfinae-incomplete' if ((d.getVar('GCCVERSION') or '').split('.')[0] or '0').isdigit() and int((d.getVar('GCCVERSION') or '0').split('.')[0]) >= 16 else ''}"
OECMAKE_SOURCEPATH = "${S}/cmake"

PACKAGECONFIG ?= "python"
PACKAGECONFIG[python] = ",,python3-numpy-native,python3-coloredlogs python3-flatbuffers python3-numpy python3-protobuf python3-sympy"

do_configure() {
    cmake_do_configure
}

do_compile() {
    cmake_do_compile
    if "${@bb.utils.contains('PACKAGECONFIG', 'python', 'true', 'false', d)}"; then
        setuptools3_do_compile
    fi
}

do_install() {
    cmake_do_install
    if "${@bb.utils.contains('PACKAGECONFIG', 'python', 'true', 'false', d)}"; then
        setuptools3_do_install
    fi
}

SOLIBS = ".so"
FILES_SOLIBSDEV = ""

PACKAGES += "python3-${PN}"
FILES:${PN} = "${bindir} ${libdir}/libonnxruntime*.so*"
FILES:${PN}-dev = " \
    ${includedir}/onnxruntime \
    ${libdir}/pkgconfig \
    ${libdir}/cmake \
"
FILES:python3-${PN} = "${PYTHON_SITEPACKAGES_DIR}"
RDEPENDS:python3-${PN} = "${PN}"

INSANE_SKIP:${PN} = "buildpaths dev-so"
INSANE_SKIP:python3-${PN} = "buildpaths"
INSANE_SKIP:${PN}-dev = "buildpaths"
INSANE_SKIP:${PN}-dbg = "buildpaths"
