def faad2_license_ok(d):
    if bb.data.inherits_class('license', d):
        license_allowedlist = (d.getVar('LICENSE_FLAGS_WHITELIST') or '').split()
        return 'commercial' in license_allowedlist or 'commercial_faad2' in license_allowedlist
    return True

PACKAGECONFIG:append:tegra = "${@' faad' if faad2_license_ok(d) else ''}"
PACKAGE_ARCH:tegra = "${@'${TEGRA_PKGARCH}' if faad2_license_ok(d) else '${TUNE_PKGARCH}'}"
