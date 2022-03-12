FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

TEGRA_HWRNG = " file://tegra-hwrng.cfg"
TEGRA_HWRNG:tegra210 = ""

SRC_URI:append:tegra = "${TEGRA_HWRNG}"

