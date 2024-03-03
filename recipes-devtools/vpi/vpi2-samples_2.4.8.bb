SUMMARY = "NVIDIA VPI command-line sample applications"
HOMEPAGE = "https://developer.nvidia.com/embedded/vpi"
LICENSE = "BSD-3-Clause & Proprietary"
LIC_FILES_CHKSUM = "file://01-convolve_2d/main.cpp;endline=27;md5=ad6efe6d8b43b8bceef6d97c7b79193f \
                    file://assets/LICENSE;md5=6b5f633fc3acaabf21035790a05b1c71"

COMPATIBLE_MACHINE = "(tegra)"

inherit l4t_deb_pkgfeed cuda cmake

SRC_COMMON_DEBS = "vpi2-samples_${PV}_arm64.deb;subdir=vpi2-samples"
SRC_URI[sha256sum] = "b47ddf20b00e0de8c4513101c3559dadd915ceb0cbf78e7ca5e45b75c6904410"

SRC_URI += "file://CMakeLists.txt;subdir=vpi2-samples/opt/nvidia/vpi2/samples"

VPI_PREFIX = "/opt/nvidia/vpi2"
EXTRA_OECMAKE = "-DCMAKE_INSTALL_PREFIX:PATH=${VPI_PREFIX}"

PACKAGECONFIG ??= "${@bb.utils.contains('LICENSE_FLAGS_ACCEPTED', 'commercial', 'video', bb.utils.contains('LICENSE_FLAGS_ACCEPTED', 'commercial_ffmpeg', 'video', '', d), d)}"
PACKAGECONFIG[video] = "-DBUILD_VIDEO_SAMPLES=ON,-DBUILD_VIDEO_SAMPLES=OFF,"

S = "${WORKDIR}/vpi2-samples/opt/nvidia/vpi2/samples"

DEPENDS = "libnvvpi2 opencv"

LDFLAGS += "-Wl,-rpath,/opt/nvidia/cupva-2.3/lib/aarch64-linux-gnu"

FILES:${PN} = "${VPI_PREFIX}"
