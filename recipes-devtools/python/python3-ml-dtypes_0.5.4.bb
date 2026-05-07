SUMMARY = "Stand-alone NumPy dtype extensions used in machine learning"
HOMEPAGE = "https://github.com/jax-ml/ml_dtypes"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=3b83ef96387f14655fc854ddc3c6bd57"

PYPI_PACKAGE = "ml_dtypes"
SRC_URI[sha256sum] = "8ab06a50fb9bf9666dd0fe5dfb4676fa2b0ac0f31ecff72a6c3af8e22c063453"

SRC_URI += "file://0001-pyproject-relax-setuptools-pin.patch"

inherit pypi python_setuptools_build_meta

DEPENDS += " \
    python3-numpy-native \
    python3-pybind11-native \
"

RDEPENDS:${PN} += " \
    python3-numpy \
"

BBCLASSEXTEND = "native nativesdk"
