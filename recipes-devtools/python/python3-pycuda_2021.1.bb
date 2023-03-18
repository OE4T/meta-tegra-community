SUMMARY = "Python wrapper for Nvidia CUDA"
HOMEPAGE = "http://mathema.tician.de/software/pycuda"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=5f3f85642a528c8fca0fe71e7a031203"

DEPENDS = "python3-setuptools-native python3-cython-native python3-cython python3-numpy-native cuda-nvprof"

SRC_URI[sha256sum] = "ab87312d0fc349d9c17294a087bb9615cffcf966ad7b115f5b051008a48dd6ed"
SRC_URI:append = " file://0001-update-nvcc-binary-path.patch"

COMPATIBLE_MACHINE = "(tegra)"

S = "${WORKDIR}/pycuda-${PV}"

inherit pypi cuda distutils3

CUDA_PATH = "/usr/local/cuda-${CUDA_VERSION}"
CFLAGS += "-I=${CUDA_PATH}/include"

do_configure() {
    # special configururation
    ${PYTHON_PN} -u ${S}/configure.py
    # disable CURAND otherwise cannot compile
    sed -i 's?CUDA_ENABLE_CURAND = True?CUDA_ENABLE_CURAND = False?g' ${B}/siteconf.py
}

RDEPENDS_${PN} += "\
    python3-appdirs \
    python3-decorator \
    python3-mako \
    python3-pytools \
    python3-core \
    python3-crypt \
    python3-io \
    python3-math \
    python3-numbers \
    python3-numpy \
    python3-pkg-resources \
    python3-py \
    python3-six \
"
