SUMMARY = "NVIDIA VPI command-line sample applications"
HOMEPAGE = "https://developer.nvidia.com/embedded/vpi"
LICENSE = "BSD-3-Clause & Proprietary"
LIC_FILES_CHKSUM = "file://01-convolve_2d/main.cpp;endline=27;md5=ad6efe6d8b43b8bceef6d97c7b79193f \
                    file://assets/LICENSE;md5=6b5f633fc3acaabf21035790a05b1c71"

COMPATIBLE_MACHINE = "(tegra)"

inherit l4t_deb_pkgfeed cuda cmake

SRC_COMMON_DEBS = "vpi3-samples_${PV}_arm64.deb;subdir=vpi3-samples"
SRC_URI[sha256sum] = "e7850f6675856507b5064c23315934a025e811ebf1264f059670730e45885eec"

SRC_URI += "file://CMakeLists.txt;subdir=vpi3-samples/opt/nvidia/vpi3/samples"

VPI_PREFIX = "/opt/nvidia/vpi3"
EXTRA_OECMAKE = "-DCMAKE_INSTALL_PREFIX:PATH=${VPI_PREFIX}"

PACKAGECONFIG ??= "${@bb.utils.contains('LICENSE_FLAGS_ACCEPTED', 'commercial', 'video', bb.utils.contains('LICENSE_FLAGS_ACCEPTED', 'commercial_ffmpeg', 'video', '', d), d)}"
PACKAGECONFIG[video] = "-DBUILD_VIDEO_SAMPLES=ON,-DBUILD_VIDEO_SAMPLES=OFF,"

S = "${UNPACKDIR}/vpi3-samples/opt/nvidia/vpi3/samples"

DEPENDS = "libnvvpi3 opencv"

LDFLAGS += "-Wl,-rpath,/opt/nvidia/cupva-2.5/lib/aarch64-linux-gnu"

FILES:${PN} = "${VPI_PREFIX}"
