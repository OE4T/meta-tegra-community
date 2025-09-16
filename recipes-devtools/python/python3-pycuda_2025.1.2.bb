SUMMARY = "Python wrapper for Nvidia CUDA"
HOMEPAGE = "http://mathema.tician.de/software/pycuda"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=8dd9e67c46dbe605fba6aeb236b48c8a"

DEPENDS = "python3-setuptools-native python3-cython-native python3-cython python3-numpy-native cuda-profiler-api boost"

SRC_URI = " \
    git://github.com/inducer/pycuda.git;protocol=https;nobranch=1;tag=v${PV} \
    git://github.com/inducer/bpl-subset;protocol=https;nobranch=1;name=bpl_subset;destsuffix=${BPN}-${PV}/bpl-subset \
    git://github.com/inducer/compyte.git;protocol=https;nobranch=1;name=compyte;destsuffix=${BPN}-${PV}/pycuda/compyte \
"
SRCREV = "7f0ca24db493c2bc92a42cb8e268c17408c45949"
SRCREV_bpl_subset = "021f0d90d2730cc138b4dd476cb3184b27c2a33e"
SRCREV_compyte = "955160ac2f504dabcd8641471a56146fa1afe35d"

SRCREV_FORMAT = "pycuda_bpl_subset_compyte"

SRC_URI:append = " file://0001-OE-cross-build-fixups.patch"

COMPATIBLE_MACHINE = "(tegra)"

inherit cuda setuptools3

do_configure() {
    # special configururation
    ${PYTHON_PN} -u ${S}/configure.py
}

CXXFLAGS += "-I${RECIPE_SYSROOT}/usr/local/cuda-${CUDA_VERSION}/include"
CFLAGS += "-I${RECIPE_SYSROOT}/usr/local/cuda-${CUDA_VERSION}/include"

RDEPENDS:${PN} += "\
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
    python3-typing-extensions \
    python3-platformdirs \
    cuda-nvcc \
"
