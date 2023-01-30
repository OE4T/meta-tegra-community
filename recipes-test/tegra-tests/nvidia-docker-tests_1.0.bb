DESCRIPTION = "Scripts for testing docker containers with PyTorch, TensorFlow, TensorRT and CUDA library"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE.md;md5=da33f1c12f6d31e71cd114d0c336d4be"

SRC_REPO ?= "github.com/dusty-nv/jetson-containers"
SRCBRANCH ?= "master"
SRC_URI = "git://${SRC_REPO};branch=${SRCBRANCH};protocol=https"
SRCREV = "bb17dc7db1ce46bb29e79ef0ac4e13aa934adf31"
PV = "1.0+git${SRCPV}"

SRC_URI += "\
    file://0001-Distro-agnostic-support-for-Test-ML-script.patch \
    file://0002-Add-version-remap-script-support.patch \
    file://l4t_version_remap.sh.in \
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
    for i in $(ls ${S}/test/*.sh); do
        install -m 0755 ${i} ${D}/opt/nvidia-docker-tests/test
    done
    install -d ${D}/opt/nvidia-docker-tests/test/data
    install -m 0644 ${S}/test/data/test_0.jpg ${D}/opt/nvidia-docker-tests/test/data

    install -d ${D}${bindir}
    install -m 0755 ${WORKDIR}/run-docker-tests.sh.in ${D}${bindir}/run-docker-tests
    install -m 0755 ${WORKDIR}/l4t_version_remap.sh.in ${D}/opt/nvidia-docker-tests/scripts/l4t_version_remap.sh
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
