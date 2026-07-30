SUMMARY = "Python Bindings for the NVIDIA Management Library"
HOMEPAGE = "https://pypi.org/project/nvidia-ml-py/"
SECTION = "devel/python"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://pynvml.py;beginline=1;endline=27;md5=5b6a2fe668739c259facb7644623b7d2"

# PEP 625 sdist uses underscores: nvidia_ml_py-13.610.43.tar.gz
PYPI_PACKAGE = "nvidia_ml_py"
SRC_URI[sha256sum] = "65437eb73d68d0c62c931ca4d45038472faff03bd0b8729abba4b899f70d60f2"

inherit pypi setuptools3

RDEPENDS:${PN} += "python3-ctypes"

BBCLASSEXTEND = "native nativesdk"
