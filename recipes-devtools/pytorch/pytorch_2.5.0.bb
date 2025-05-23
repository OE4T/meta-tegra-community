SUMMARY = "Tensors and Dynamic neural network computation with strong GPU acceleration"
HOMEPAGE = "https://pytorch.org/"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=931408ebbfa3b49f31a563bce9755581"

SRC_URI = "https://github.com/pytorch/pytorch/releases/download/v${PV}/pytorch-v${PV}.tar.gz"
SRC_URI[sha256sum] = "91b3ba7db9cd6ac48f672a615872dcf1e4c29783d9c615336b11553915d5298a"

SRC_URI += " \
    file://0001-Remove-Modules_CUDA_fix.patch \
    file://0002-Fix-CUDA-build-rules.patch \
    file://0003-Fix-RPATH.patch \
    file://0004-Use-native-protobuf-compiler.patch \
    file://0005-Disable-various-warnings.patch \
    file://0006-Fixups-for-cross-building-in-OE.patch \
"

S = "${UNPACKDIR}/${PN}-v${PV}"

COMPATIBLE_MACHINE = "(cuda)"

inherit cmake cuda

EXTRA_OECMAKE += " \
    -DGLIBCXX_USE_CXX11_ABI=1 \
    -DBUILD_PYTHON=0 \
    -DBUILD_CUSTOM_PROTOBUF=0 \
    -DUSE_OPENMP=OFF \
    -DUSE_XNNPACK=OFF \
    -DUSE_NCCL=OFF \
    -DUSE_CUSPARSELT=OFF \
    -DPROTOBUF_PROTOC_EXECUTABLE=${STAGING_BINDIR_NATIVE}/protoc \
    -DUSE_SYSTEM_PYBIND11=ON \
"

# Disable installing the fmt third-party library, which may cause conflicts
# with other components that depend on PyTorch.
EXTRA_OECMAKE += " \
    -DFMT_INSTALL=OFF \
"

# Set flags required for CUDA compilation (which have been removed from
# the problematic CMake files by the patches included above).
CUDA_NVCC_EXTRA_FLAGS += " \
    --expt-relaxed-constexpr \
    --expt-extended-lambda \
    -Xfatbin=-compress-all \
    -DCUDA_HAS_FP16=1 \
    -D__CUDA_NO_HALF_OPERATORS__ \
    -D__CUDA_NO_HALF_CONVERSIONS__ \
    -D__CUDA_NO_HALF2_OPERATORS__ \
    -D__CUDA_NO_BFLOAT16_CONVERSIONS__ \
    ${@'-DCUB_WRAPPED_NAMESPACE=at_cuda_detail' if float(d.getVar('CUDA_VERSION', True) or '0') > 11.4 else ''} \
    -DLIBCUDACXX_ENABLE_SIMPLIFIED_COMPLEX_OPERATIONS \
"

# Suppress various CUDA warnings.
CUDA_DIAG_SUPPRESS = " \
    cc_clobber_ignored \
    integer_sign_change \
    useless_using_declaration \
    set_but_not_used \
    field_without_dll_interface \
    base_class_has_different_dll_interface \
    dll_interface_conflict_none_assumed \
    dll_interface_conflict_dllexport_assumed \
    implicit_return_from_non_void_function \
    unsigned_compare_with_zero \
    declared_but_not_referenced \
    bad_friend_decl \
"
CUDA_NVCC_EXTRA_FLAGS += "${@' '.join(['-Xcudafe "--diag_suppress=%s"' % s for s in d.getVar('CUDA_DIAG_SUPPRESS').split()])}"

# Fix the "#include_next <math.h>" compilation error when building CUDA files.
EXTRA_OECMAKE += " \
    -DCMAKE_CUDA_IMPLICIT_INCLUDE_DIRECTORIES=${STAGING_INCDIR} \
"

# Generate the sleef binaries used at build time.
EXTRA_OECMAKE += " -DNATIVE_BUILD_DIR=${STAGING_DIR_NATIVE}/usr"
do_compile:prepend() {
    ${BUILD_CC} -o ${STAGING_BINDIR_NATIVE}/mkalias ${S}/third_party/sleef/src/libm/mkalias.c
    ${BUILD_CC} -o ${STAGING_BINDIR_NATIVE}/mkrename ${S}/third_party/sleef/src/libm/mkrename.c
    ${BUILD_CC} -o ${STAGING_BINDIR_NATIVE}/mkdisp ${S}/third_party/sleef/src/libm/mkdisp.c
}

# PyTorch builds tend to fail or crash with too many threads.
# Limit the thread count to 1/4 of the CPUs available.
CMAKE_BUILD_PARALLEL_LEVEL:task-compile = \
    "${@ min(int(d.getVar('PARALLEL_MAKE').split()[1]), int(oe.utils.cpu_count() / 4))}"

DEPENDS += " \
    cuda-nvml \
    cuda-nvrtc \
    cuda-nvtx \
    cudnn \
    protobuf \
    protobuf-native \
"

FILES:${PN} += " \
    ${datadir}/ATen \
    ${datadir}/cpuinfo \
"

SOLIBS = "*.so*"
FILES_SOLIBSDEV = ""

# The build flags (including local build paths) are recorded within the
# library and generated headers. This is harmless and can be ignored.
INSANE_SKIP:${PN} += "buildpaths"
INSANE_SKIP:${PN}-dev += "buildpaths"
INSANE_SKIP:${PN}-src += "buildpaths"
