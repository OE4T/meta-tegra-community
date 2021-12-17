FILESEXTRAPATHS:prepend:tegra := "${THISDIR}/${BPN}:"
SRC_URI:append:tegra = " file://0001-Temp-workaround-patch-openssl-3.patch"
