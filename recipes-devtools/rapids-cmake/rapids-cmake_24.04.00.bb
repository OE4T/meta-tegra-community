DESCRIPTION = "A collection of CMake modules that are useful for all CUDA RAPIDS projects"
HOMEPAGE = "https://github.com/rapidsai/rapids-cmake"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=3ced5b165b4647193d419f133b7461c2"

SRC_URI = " \
    git://gitlab.com/nvidia/rapidsai/rapids-cmake.git;protocol=https;nobranch=1;tag=v${PV} \
    file://0001-Update-CPM.cmake-md5sum.patch \
"
SRCREV = "0d6b5a59184f9c70c611349cc63dfdbf28cf8ded"

do_configure[noexec] = "1"
do_compile[noexec] = "1"

do_install() {
    install -d ${D}/opt/nvidia/rapids-cmake
    cp -R --preserve=mode,links,timestamps ${S}/* ${D}/opt/nvidia/rapids-cmake
}

ALLOW_EMPTY:${PN} = "1"
SYSROOT_DIRS:append = " /opt/nvidia/rapids-cmake"

FILES:${PN}-dev += "/opt/nvidia/rapids-cmake"
RDEPENDS:${PN}-dev += "bash"

INSANE_SKIP:${PN}-dev += "staticdev"
