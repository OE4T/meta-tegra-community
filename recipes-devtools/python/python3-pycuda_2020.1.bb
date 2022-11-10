SUMMARY = "Python wrapper for Nvidia CUDA"
HOMEPAGE = "http://mathema.tician.de/software/pycuda"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=5f3f85642a528c8fca0fe71e7a031203"

DEPENDS = "python3-setuptools-native python3-cython-native python3-cython python3-numpy-native"

SRC_URI += "file://0001-Drop-obsolete-profiler-interface.patch"
SRC_URI[md5sum] = "b8ef6b246f04ad431a6df61e1af0319f"
SRC_URI[sha256sum] = "effa3b99b55af67f3afba9b0d1b64b4a0add4dd6a33bdd6786df1aa4cc8761a5"

COMPATIBLE_MACHINE = "(tegra)"

S = "${WORKDIR}/pycuda-${PV}"

inherit pypi cuda setuptools3

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
"

do_configure() {
    # special configururation
    ${PYTHON_PN} -u ${S}/configure.py
    # disable CURAND otherwise cannot compile
    sed -i 's?CUDA_ENABLE_CURAND = True?CUDA_ENABLE_CURAND = False?g' ${B}/siteconf.py
}
