FILESEXTRAPATHS:prepend:tegra := "${THISDIR}/${BPN}:"
SRC_URI:append:tegra = " file://0001-Temp-workaround-patch-openssl-3.patch"
SRC_URI:append:tegra = " file://0002-Prevent-rebuilding-during-install.patch"
EXTRA_OEMAKE:append:tegra = ' VERSION="${PV}"'
