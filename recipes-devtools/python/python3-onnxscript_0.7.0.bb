SUMMARY = "Naturally author ONNX functions and models using a subset of Python"
HOMEPAGE = "https://microsoft.github.io/onnxscript/"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=0f7e3b1308cb5c00b372a6e78835732d"

PYPI_PACKAGE = "onnxscript"
SRC_URI[sha256sum] = "c95ed7b339b02cface56ee27689565c46612e1fc542c562298dddfdad5268dc5"

inherit pypi python_setuptools_build_meta

# Without this, setup.py appends ".dev<today>" to the version read from the
# VERSION file, which would make the built artifact version mismatch ${PV}.
export ONNX_SCRIPT_RELEASE = "1"

RDEPENDS:${PN} += " \
    python3-onnx-ir \
    python3-onnx \
    python3-numpy \
    python3-ml-dtypes \
    python3-packaging \
    python3-typing-extensions \
"

BBCLASSEXTEND = "native nativesdk"
