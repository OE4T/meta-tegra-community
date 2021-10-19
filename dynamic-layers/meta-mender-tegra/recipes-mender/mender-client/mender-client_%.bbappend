FILESEXTRAPATHS:prepend:tegra := "${THISDIR}/${BPN}:"
SRC_URI:append:tegra = " file://0001-Temp-workaround-patch-openssl-3.patch"
EXTRAEXTRADEPS:tegra = " tegra-boot-tools-nvbootctrl tegra-boot-tools-lateboot"
EXTRAEXTRADEPS:tegra210 = ""
EXTRADEPS:append:tegra = "${EXTRAEXTRADEPS}"

