SUMMARY = "NVIDIA VPI command-line sample applications"
HOMEPAGE = "https://developer.nvidia.com/embedded/vpi"
LICENSE = "BSD-3-Clause AND LicenseRef-Proprietary"
LIC_FILES_CHKSUM = "file://01-convolve_2d/main.cpp;endline=27;md5=ac7975b528d3b5d7eb2f4ddcb629e547 \
                    file://assets/LICENSE;md5=6b5f633fc3acaabf21035790a05b1c71"

COMPATIBLE_MACHINE = "(tegra)"

inherit l4t_deb_pkgfeed cuda cmake

SRC_COMMON_DEBS = "vpi4-samples_${PV}_arm64.deb;subdir=vpi4-samples"
SRC_URI[sha256sum] = "230f18fc33cf5f135e23a83640952b9ad4e77e4e97554da10c95bd0b8a201eb4"

SRC_URI += "file://CMakeLists.txt;subdir=vpi4-samples/opt/nvidia/vpi4/samples"

VPI_PREFIX = "/opt/nvidia/vpi4"
EXTRA_OECMAKE = "-DCMAKE_INSTALL_PREFIX:PATH=${VPI_PREFIX}"

PACKAGECONFIG ??= "${@bb.utils.contains('LICENSE_FLAGS_ACCEPTED', 'commercial', 'video', bb.utils.contains('LICENSE_FLAGS_ACCEPTED', 'commercial_ffmpeg', 'video', '', d), d)}"
PACKAGECONFIG[video] = "-DBUILD_VIDEO_SAMPLES=ON,-DBUILD_VIDEO_SAMPLES=OFF,"

S = "${UNPACKDIR}/vpi4-samples/opt/nvidia/vpi4/samples"

DEPENDS = "libnvvpi4 opencv"

RDEPENDS:${PN} += "tegra-libraries-pva"

FILES:${PN} = "${VPI_PREFIX}"
