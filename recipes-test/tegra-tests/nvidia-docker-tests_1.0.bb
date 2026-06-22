DESCRIPTION = "Test that the NVIDIA container runtime injects the host driver files and runs a CUDA workload in a container"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = "file://run-docker-tests.sh.in"

S = "${UNPACKDIR}"

COMPATIBLE_MACHINE = "(tegra)"

do_install() {
    install -d ${D}${bindir}
    sed 's/@CUDA_VERSION@/${CUDA_VERSION}/' ${UNPACKDIR}/run-docker-tests.sh.in > ${D}${bindir}/run-docker-tests
    chmod 0755 ${D}${bindir}/run-docker-tests
}

FILES:${PN} = " \
    ${bindir}/run-docker-tests \
"
RDEPENDS:${PN} = " \
    docker-moby \
    nvidia-container-toolkit \
    tegra-configs-container-csv \
    cuda-samples \
"
