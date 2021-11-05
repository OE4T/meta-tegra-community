SUMMARY = "Easy to use Python camera interface for NVIDIA Jetson"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE.md;md5=295ba21d3c8eb4396383d735495fbe6e"

COMPATIBLE_MACHINE = "(tegra)"

inherit setuptools3

SRC_URI = "git://github.com/NVIDIA-AI-IOT/jetcam.git;protocol=https;branch=master"
SRCREV = "508ff3a402c5a0449a8d5776c05d2d17d6a87f46"
PV = "git${SRCPV}"

S = "${WORKDIR}/git"

RDEPENDS:${PN} += "python3-numpy \
                   python3-traitlets \
                   python3-opencv \
                   gstreamer1.0-plugins-nvvidconv \
                   gstreamer1.0-plugins-nvarguscamerasrc"
