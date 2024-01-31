SUMMARY = "Python client for the Prometheus monitoring system"
HOMEPAGE = "https://github.com/prometheus/client_python"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=86d3f3a95c324c9479bd8986968f4327"

SRC_URI = "git://github.com/prometheus/client_python.git;protocol=https;branch=master"
SRCREV = "b3541ac5f005bc4eb82eddc1a55a63b711c0b95c"

COMPATIBLE_MACHINE = "(tegra)"

S = "${WORKDIR}/git"

inherit setuptools3
