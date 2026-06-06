SUMMARY = "NVIDIA VPI command-line sample applications"
HOMEPAGE = "https://developer.nvidia.com/embedded/vpi"
LICENSE = "BSD-3-Clause & Proprietary"
LIC_FILES_CHKSUM = "file://01-convolve_2d/main.cpp;endline=27;md5=ad6efe6d8b43b8bceef6d97c7b79193f \
                    file://assets/LICENSE;md5=6b5f633fc3acaabf21035790a05b1c71"

COMPATIBLE_MACHINE = "(tegra)"

inherit l4t_deb_pkgfeed cuda cmake

SRC_COMMON_DEBS = "vpi4-samples_${PV}_arm64.deb;subdir=vpi4-samples"
SRC_URI[sha256sum] = "1ee39ddf269c64557e71a16cafa5a9d7cd7d78154f648781185d6a86f8973a55"

SRC_URI += "file://CMakeLists.txt;subdir=vpi4-samples/opt/nvidia/vpi4/samples"

VPI_PREFIX = "/opt/nvidia/vpi4"
EXTRA_OECMAKE = "-DCMAKE_INSTALL_PREFIX:PATH=${VPI_PREFIX}"

PACKAGECONFIG ??= "${@bb.utils.contains('LICENSE_FLAGS_ACCEPTED', 'commercial', 'video', bb.utils.contains('LICENSE_FLAGS_ACCEPTED', 'commercial_ffmpeg', 'video', '', d), d)}"
PACKAGECONFIG[video] = "-DBUILD_VIDEO_SAMPLES=ON,-DBUILD_VIDEO_SAMPLES=OFF,"

S = "${UNPACKDIR}/vpi4-samples/opt/nvidia/vpi4/samples"

DEPENDS = "libnvvpi4 opencv"

RDEPENDS:${PN} += "tegra-libraries-pva"

FILES:${PN} = "${VPI_PREFIX}"
