SUMMARY = "Tensors and Dynamic neural network computation with strong GPU acceleration"
HOMEPAGE = "https://pytorch.org/"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=b114fbe63fdb5f7a91332a4aefb61ee5"

# cuda-cupti provides CUPTI for the Kineto profiler backend. Since PyTorch 2.13
# (third_party/kineto), building with USE_CUDA forces KINETO_BACKEND=cuda, whose
# CMake hard-fails if the CUDA::cupti target is missing. FindCUDAToolkit locates
# cupti.h under the *native* toolkit root (extras/CUPTI/include), then resolves
# libcupti.so from the *target* sysroot, so CUPTI is required in both sysroots --
# the same native+target split already used for cudart.
DEPENDS += " \
    cuda-cupti \
    cuda-cupti-native \
    cuda-nvml \
    cuda-nvrtc \
    cuda-nvtx \
    cudnn \
    libcusparse \
    onnx \
    protobuf \
    protobuf-native \
    python3-pybind11 \
    python3-pyyaml-native \
    python3-typing-extensions-native \
"

SRC_URI = "https://github.com/pytorch/pytorch/releases/download/v${PV}/pytorch-v${PV}.tar.gz"
SRC_URI[sha256sum] = "66614a19060f69cfd63cd0295f65a1241bd15df2fa65c60ae51066c11c2ce812"

SRC_URI += " \
    file://0001-Fix-generate-linker-script-for-cross-compilation.patch \
    file://0002-Fix-CUDA-build-rules-for-cross-compilation.patch \
    file://0003-Use-native-protobuf-compiler.patch \
    file://0004-Prepend-Modules_CUDA_fix-to-CMAKE_MODULE_PATH.patch \
    file://0005-Fix-FMT-installation.patch \
    file://0006-PostBuildSteps-honor-DESTDIR-for-install-time-script.patch \
    file://0007-Route-Python-package-data-via-SKBUILD_PLATLIB_DIR.patch \
"

S = "${UNPACKDIR}/${PN}-v${PV}"

COMPATIBLE_MACHINE = "(cuda)"

inherit cmake cuda python3native python3-dir

PACKAGECONFIG ??= "numpy openmp python"
PACKAGECONFIG[numpy] = "-DUSE_NUMPY=1, -DUSE_NUMPY=0, python3-numpy python3-numpy-native, python3-numpy"
PACKAGECONFIG[openmp] = "-DUSE_OPENMP=ON, -DUSE_OPENMP=OFF"
PACKAGECONFIG[python] = "-DBUILD_PYTHON=1, -DBUILD_PYTHON=0, cuda-profiler-api, pytorch python3-typing-extensions"

EXTRA_OECMAKE += " \
    -DGLIBCXX_USE_CXX11_ABI=1 \
    -DBUILD_CUSTOM_PROTOBUF=0 \
    -DUSE_XNNPACK=OFF \
    -DUSE_NCCL=OFF \
    -DUSE_CUSPARSELT=OFF \
    -DBUILD_TEST=OFF \
    -Dnvtx3_dir=${S}/third_party/NVTX/c/include \
    -DPROTOBUF_PROTOC_EXECUTABLE=${STAGING_BINDIR_NATIVE}/protoc \
    -DUSE_SYSTEM_ONNX=ON \
    -DUSE_SYSTEM_PYBIND11=ON \
    -DTORCH_CUDA_ARCH_LIST=${@' '.join(['%s.%s' % (a[:-1], a[-1]) for a in d.getVar('CUDA_ARCHITECTURES').split()])} \
"

# Disable installing the fmt third-party library, which may cause conflicts
# with other components that depend on PyTorch.
EXTRA_OECMAKE += " \
    -DFMT_INSTALL=OFF \
"

# PyTorch 2.13 installs the Python package data (type stubs, _inductor
# templates, torchgen/packaged codegen data, ...) from CMake, using a wheel
# layout rooted at CMAKE_INSTALL_PREFIX and SKBUILD_PLATLIB_DIR. We keep the
# C++ runtime/headers in the system prefix (/usr), so point SKBUILD_PLATLIB_DIR
# at site-packages (together with patch 0007) to route that Python data into
# ${PYTHON_SITEPACKAGES_DIR}/torch and torchgen instead of scattering it across
# /usr and the source tree.
EXTRA_OECMAKE += " \
    -DSKBUILD_PLATLIB_DIR=${PYTHON_SITEPACKAGES_DIR} \
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

# Build the torch._C Python extension from stub.c, which calls into libtorch_python.so.
# In the upstream build this is handled by setuptools in setup.py:configure_extension_build()
# (see setup.py lines ~1546-1626: Extension("torch._C", sources=["torch/csrc/stub.c"],
# libraries=["torch_python"])). We replicate it here with the cross-compiler since we do
# not run setup.py.
do_compile:append() {
    if ${@bb.utils.contains('PACKAGECONFIG', 'python', 'true', 'false', d)}; then
        PYEXTSUFFIX="$(sed -n "s/.*'EXT_SUFFIX': '\([^']*\)'.*/\1/p" ${STAGING_LIBDIR}/${PYTHON_DIR}/_sysconfigdata*.py | head -1)"
        ${CC} ${LDFLAGS} -shared -fPIC \
            -I${STAGING_INCDIR}/${PYTHON_DIR} \
            ${S}/torch/csrc/stub.c \
            -L${B}/lib -ltorch_python \
            -o ${B}/_C${PYEXTSUFFIX}
    fi
}

# Install the Python package (torch and torchgen) into site-packages.
# This replicates what setup.py does via setuptools when run upstream:
#   - torch/ and torchgen/ packages: setup.py:configure_extension_build() lines ~1615-1622
#     via find_packages(include=["torch", "torch.*", "torchgen", "torchgen.*"])
#   - version.py: generated by cmake target gen_torch_version in torch/CMakeLists.txt
#     lines ~486-498 via tools/generate_torch_version.py (already in ${S}/torch/version.py)
#   - torch/lib/ and torch/bin/: setup.py package_data lines ~1739,1781-1790
#     ("bin/*", "lib/*.so*"); we use symlinks since the libraries live in ${libdir}
do_install:append() {
    if ! ${@bb.utils.contains('PACKAGECONFIG', 'python', 'true', 'false', d)}; then
        return
    fi

    PYEXTSUFFIX="$(sed -n "s/.*'EXT_SUFFIX': '\([^']*\)'.*/\1/p" ${STAGING_LIBDIR}/${PYTHON_DIR}/_sysconfigdata*.py | head -1)"

    # Install Python package: torch/
    # PyTorch 2.13's cmake install already staged the package data (type stubs,
    # _inductor jinja templates, benchmark/valgrind headers, version.py) into
    # site-packages/torch via SKBUILD_PLATLIB_DIR (see patch 0007), so the
    # directory already exists. Merge the .py sources into it (copy contents,
    # not the directory) to avoid nesting torch/torch.
    install -d ${D}${PYTHON_SITEPACKAGES_DIR}/torch
    cp -R --no-dereference --preserve=mode,timestamps ${S}/torch/. ${D}${PYTHON_SITEPACKAGES_DIR}/torch/

    # version.py.tpl is the template; version.py is already generated by cmake
    rm -f ${D}${PYTHON_SITEPACKAGES_DIR}/torch/version.py.tpl

    # Install Python package: torchgen/ (merge sources with the cmake-mirrored
    # torchgen/packaged codegen data already staged by SKBUILD_PLATLIB_DIR)
    install -d ${D}${PYTHON_SITEPACKAGES_DIR}/torchgen
    cp -R --no-dereference --preserve=mode,timestamps ${S}/torchgen/. ${D}${PYTHON_SITEPACKAGES_DIR}/torchgen/

    # FileMirroring.cmake stages tools/shared/_utils_internal.py (a build-time
    # symlink-replacement copy) as a top-level site-packages/tools package. It
    # is not needed at runtime; drop it to avoid polluting the target namespace.
    rm -rf ${D}${PYTHON_SITEPACKAGES_DIR}/tools

    # Remove __pycache__ directories: the .pyc files contain host build paths.
    # Python will regenerate them on the target at first import.
    find ${D}${PYTHON_SITEPACKAGES_DIR} -depth -name __pycache__ -exec rm -rf {} +

    # Replace the source-tree lib/ (which has libshm C++ source) with a directory
    # containing only the symlinks that torch/__init__.py needs at runtime.
    rm -rf ${D}${PYTHON_SITEPACKAGES_DIR}/torch/lib
    install -d ${D}${PYTHON_SITEPACKAGES_DIR}/torch/lib
    # torch/__init__.py loads libtorch_global_deps.so from torch/lib/ via ctypes.CDLL
    ln -sf ${libdir}/libtorch_global_deps.so \
        ${D}${PYTHON_SITEPACKAGES_DIR}/torch/lib/libtorch_global_deps.so

    # torch/__init__.py resolves torch_shm_manager via get_file_path("torch", "bin", ...)
    install -d ${D}${PYTHON_SITEPACKAGES_DIR}/torch/bin
    ln -sf ${bindir}/torch_shm_manager \
        ${D}${PYTHON_SITEPACKAGES_DIR}/torch/bin/torch_shm_manager

    # Install the torch._C extension module built from stub.c
    install -m 0755 ${B}/_C${PYEXTSUFFIX} \
        ${D}${PYTHON_SITEPACKAGES_DIR}/torch/_C${PYEXTSUFFIX}

    # CMake installs a plain _C.so to ${prefix}/ (the wheel root); the correctly
    # ABI-tagged copy is already above. Drop the stray to satisfy installed-vs-shipped.
    rm -f ${D}${prefix}/_C.so
}

PACKAGES =+ "${@bb.utils.contains('PACKAGECONFIG', 'python', 'python3-torch-dev python3-torch', '', d)}"

SOLIBS = "*.so*"
FILES_SOLIBSDEV = ""
FILES:${PN}-dev += " \
    ${datadir}/ATen \
    ${datadir}/cpuinfo \
"
FILES:python3-torch-dev = " \
    ${PYTHON_SITEPACKAGES_DIR}/torch/csrc \
    ${PYTHON_SITEPACKAGES_DIR}/torch/*.h \
    ${PYTHON_SITEPACKAGES_DIR}/torch/CMakeLists.txt \
    ${PYTHON_SITEPACKAGES_DIR}/torch/header_only_apis.txt \
"
FILES:python3-torch = " \
    ${libdir}/libtorch_python.so* \
    ${PYTHON_SITEPACKAGES_DIR}/torch \
    ${PYTHON_SITEPACKAGES_DIR}/torchgen \
"
# Python runtime dependencies. The base `import torch` works without these, but
# the torch.export/dynamo-based ONNX exporter (default since torch 2.9) and
# torch._dynamo pull them in: filelock/fsspec/jinja2/networkx/sympy/typing-
# extensions are torch's declared install_requires, and python3-modules provides
# the full stdlib (cProfile, getpass, ...) that torch._dynamo imports but which
# the minimal python3 split would otherwise omit.
RDEPENDS:python3-torch += " \
    python3-modules \
    python3-filelock \
    python3-fsspec \
    python3-jinja2 \
    python3-networkx \
    python3-sympy \
    python3-typing-extensions \
"

FILES:${PN}-staticdev += "${libdir}/mimalloc-2.3"

# The build flags (including local build paths) are recorded within the
# library and generated headers. This is harmless and can be ignored.
INSANE_SKIP:${PN} += "buildpaths"
INSANE_SKIP:${PN}-dev += "buildpaths"
# torch/__init__.py explicitly loads libtorch_global_deps.so via ctypes.CDLL with
# RTLD_GLOBAL before importing _C. PyTorch uses unversioned sonames so the symlink
# in torch/lib/ necessarily ends in .so, which triggers the dev-so check.
INSANE_SKIP:python3-torch += "dev-so"
