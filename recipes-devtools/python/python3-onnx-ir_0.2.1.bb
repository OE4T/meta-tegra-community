SUMMARY = "Efficient in-memory representation for ONNX"
HOMEPAGE = "https://onnx.ai/ir-py"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=3b83ef96387f14655fc854ddc3c6bd57"

PYPI_PACKAGE = "onnx_ir"
SRC_URI[sha256sum] = "8b8b10a93f43e65962104de6070c43c5dacb0e3cdfefc7c8059dd83c9db64f35"

inherit pypi python_setuptools_build_meta

RDEPENDS:${PN} += " \
    python3-numpy \
    python3-onnx \
    python3-typing-extensions \
    python3-ml-dtypes \
    python3-sympy \
"

BBCLASSEXTEND = "native nativesdk"
