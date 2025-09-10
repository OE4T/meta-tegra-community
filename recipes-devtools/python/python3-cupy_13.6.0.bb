SUMMARY = "NumPy/SciPy-compatible Array Library for GPU-accelerated Computing with Python"
HOMEPAGE = "https://cupy.dev/"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=6b47a0b47b880f6f283bbed9af6176e5"

SRC_URI = " \
    git://github.com/cupy/cupy.git;protocol=https;nobranch=1;tag=v${PV} \
    git://github.com/NVIDIA/cccl.git;protocol=https;nobranch=1;name=cccl;destsuffix=${BPN}-${PV}/third_party/cccl \
"
SRCREV = "25e552d5d679dcdc6f7cc3d81310a9b265463137"
SRCREV_cccl = "3a388b7b01512d48474b98389a3e776c8d8f817a"

SRCREV_FORMAT = "cupy_cccl"

SRC_URI:append = " file://0001-Fixups-for-cross-building-in-OE.patch"

DEPENDS += " \
    jitify cuda-profiler-api cuda-cudart cuda-nvrtc cuda-nvtx \
    cuda-cccl libcublas libcufft libcurand libcusparse nccl \
    dlpack python3-cython-native python3-fastrlock-native python3-numpy-native \
"

COMPATIBLE_MACHINE = "(cuda)"

inherit python_setuptools_build_meta cuda

do_compile() {
    export NVCC="${CUDA_NVCC_EXECUTABLE} -ccbin ${CUDAHOSTCXX} ${CUDAFLAGS}"
    export CUPY_NVCC_GENERATE_CODE=""
    export CUDA_VERSION="${CUDA_VERSION}"
    export VERBOSE=1
    python_pep517_do_compile
}

RDEPENDS:${PN} += " \
    python3-numpy \
    python3-fastrlock \
    bash \
"

INSANE_SKIP:${PN} = "buildpaths"
INSANE_SKIP:${PN}-src = "buildpaths"
