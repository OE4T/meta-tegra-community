FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI:append := "  \
    file://nvbootctrl-rauc \
    file://post-install \
"

RAUC_USE_DEVEL_VERSION = "1"

# nooelint: oelint.append.protvars.SRCREV
SRCREV = "3ad4979d7cef1b0596707688c6224b1806989df3"

RDEPENDS:${PN} += "tegra-redundant-boot"

do_install:append() {
    install -m 755 ${WORKDIR}/nvbootctrl-rauc ${D}${bindir}
    install -m 755 ${WORKDIR}/post-install ${D}${bindir}
}
