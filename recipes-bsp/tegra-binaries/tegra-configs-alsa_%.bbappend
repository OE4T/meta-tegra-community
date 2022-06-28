FILESEXTRAPATHS:prepend := "${THISDIR}/tegra-configs-alsa:"

SRC_URI:append = "\
    file://asound-xavier-nx.conf \
"

TEGRA_AUDIO_CONFIG:jetson-xavier-nx-devkit ?= "${WORKDIR}/asound-xavier-nx.conf"
TEGRA_AUDIO_CONFIG:jetson-xavier-nx-devkit-emmc ?= "${WORKDIR}/asound-xavier-nx.conf"
