RRECOMMENDS:${PN}-sshd:remove:tegra = "rng-tools"
RRECOMMENDS:${PN}-sshd:append:tegra = " haveged"
PACKAGE_ARCH:tegra = "${TEGRA_PKGARCH}"
