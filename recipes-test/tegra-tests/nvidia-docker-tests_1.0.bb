DESCRIPTION = "Scripts for testing docker containers with PyTorch, TensorFlow, TensorRT and CUDA library"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE.md;md5=da33f1c12f6d31e71cd114d0c336d4be"

SRC_REPO ?= "github.com/dusty-nv/jetson-containers"
SRCBRANCH ?= "master"
SRC_URI = "git://${SRC_REPO};branch=${SRCBRANCH};protocol=https"
SRCREV = "39496f8eba51ababb0cfb625fc70410163e4fe43"
PV = "1.0+git${SRCPV}"

SRC_URI += "\
        file://0001-Distro-agnostic-support-for-Test-ML-script.patch \
	file://run-docker-tests.sh.in \
"

COMPATIBLE_MACHINE = "(tegra)"

S = "${WORKDIR}/git"

do_install() {
    # Install scripts in /opt
    install -d ${D}/opt/nvidia-docker-tests/scripts
    install -m 0755 ${S}/scripts/l4t_version.sh ${D}/opt/nvidia-docker-tests/scripts
    install -m 0755 ${S}/scripts/docker_run.sh ${D}/opt/nvidia-docker-tests/scripts
    install -m 0755 ${S}/scripts/docker_test_ml.sh ${D}/opt/nvidia-docker-tests/scripts
    install -d ${D}/opt/nvidia-docker-tests/test
    for i in $(ls ${S}/test/*.py); do
        install ${i} ${D}/opt/nvidia-docker-tests/test
    done
    install -d ${D}/opt/nvidia-docker-tests/test/data
    install -m 0644 ${S}/test/data/test_0.jpg ${D}/opt/nvidia-docker-tests/test/data

    install -d ${D}${bindir}
    install -m 0755 ${WORKDIR}/run-docker-tests.sh.in ${D}${bindir}/run-docker-tests
}

FILES:${PN} = " \
                /opt/nvidia-docker-tests \
		${bindir}/run-docker-tests \
	      "
RDEPENDS:${PN} = " \
                   bash \
                   nvidia-docker \
		   nv-tegra-release \
		   "
