python () {
    if 'tegra' not in d.getVar('MACHINEOVERRIDES').split(':'):
        return
    if bb.data.inherits_class('license', d):
        license_allowedlist = (d.getVar('LICENSE_FLAGS_WHITELIST') or '').split()
        if 'commercial' not in license_allowedlist and 'commercial_faad2' not in license_allowedlist:
            return

    d.appendVar('PACKAGECONFIG', ' faad')
    d.setVar('PACKAGE_ARCH', '${TEGRA_PKGARCH}')
}
