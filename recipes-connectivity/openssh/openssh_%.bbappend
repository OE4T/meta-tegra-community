RRECOMMENDS:${PN}-sshd:remove:tegra210 = "rng-tools"
RRECOMMENDS:${PN}-sshd:append:tegra210 = " haveged"
PACKAGE_ARCH:tegra210 = "${TEGRA_PKGARCH}"
