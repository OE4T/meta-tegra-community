SUMMARY = "Secure Socket Layer"
DESCRIPTION = "Secure Socket Layer (SSL) binary and related cryptographic tools."
HOMEPAGE = "http://www.openssl.org/"
BUGTRACKER = "http://www.openssl.org/news/vulnerabilities.html"
SECTION = "libs/network"

# "openssl" here actually means both OpenSSL and SSLeay licenses apply
# (see meta/files/common-licenses/OpenSSL to which "openssl" is SPDXLICENSEMAPped)
LICENSE = "OpenSSL"
LIC_FILES_CHKSUM = "file://LICENSE;md5=d343e62fc9c833710bbbed25f27364c8"

DEPENDS = "hostperl-runtime-native"

SRC_URI = "http://www.openssl.org/source/openssl-${PV}.tar.gz \
           file://run-ptest \
           file://0001-skip-test_symbol_presence.patch \
           file://0001-buildinfo-strip-sysroot-and-debug-prefix-map-from-co.patch \
           file://afalg.patch \
           file://reproducible.patch \
           "

SRC_URI:append:class-nativesdk = " \
           file://environment.d-openssl.sh \
           "

SRC_URI[sha256sum] = "e2f8d84b523eecd06c7be7626830370300fbcc15386bf5142d72758f6963ebc6"

inherit lib_package multilib_header multilib_script ptest
MULTILIB_SCRIPTS = "${PN}-bin:${bindir}/c_rehash"

PACKAGECONFIG ?= ""
PACKAGECONFIG:class-native = ""
PACKAGECONFIG:class-nativesdk = ""

PACKAGECONFIG[cryptodev-linux] = "enable-devcryptoeng,disable-devcryptoeng,cryptodev-linux,,cryptodev-module"

S = "${WORKDIR}/openssl-${PV}"
B = "${WORKDIR}/build"
do_configure[cleandirs] = "${B}"

DISABLE_STATIC = ""

#| ./libcrypto.so: undefined reference to `getcontext'
#| ./libcrypto.so: undefined reference to `setcontext'
#| ./libcrypto.so: undefined reference to `makecontext'
EXTRA_OECONF:append:libc-musl = " no-async"
EXTRA_OECONF:append:libc-musl:powerpc64 = " no-asm"

# adding devrandom prevents openssl from using getrandom() which is not available on older glibc versions
# (native versions can be built with newer glibc, but then relocated onto a system with older glibc)
EXTRA_OECONF:class-native = "--with-rand-seed=os,devrandom"
EXTRA_OECONF:class-nativesdk = "--with-rand-seed=os,devrandom"

# Relying on hardcoded built-in paths causes openssl-native to not be relocateable from sstate.
CFLAGS:append:class-native = " -DOPENSSLDIR=/not/builtin -DENGINESDIR=/not/builtin"
CFLAGS:append:class-nativesdk = " -DOPENSSLDIR=/not/builtin -DENGINESDIR=/not/builtin"

do_configure () {
	os=${HOST_OS}
	case $os in
	linux-gnueabi |\
	linux-gnuspe |\
	linux-musleabi |\
	linux-muslspe |\
	linux-musl )
		os=linux
		;;
	*)
		;;
	esac
	target="$os-${HOST_ARCH}"
	case $target in
	linux-arm*)
		target=linux-armv4
		;;
	linux-aarch64*)
		target=linux-aarch64
		;;
	linux-i?86 | linux-viac3)
		target=linux-x86
		;;
	linux-gnux32-x86_64 | linux-muslx32-x86_64 )
		target=linux-x32
		;;
	linux-gnu64-x86_64)
		target=linux-x86_64
		;;
	linux-mips | linux-mipsel)
		# specifying TARGET_CC_ARCH prevents openssl from (incorrectly) adding target architecture flags
		target="linux-mips32 ${TARGET_CC_ARCH}"
		;;
	linux-gnun32-mips*)
		target=linux-mips64
		;;
	linux-*-mips64 | linux-mips64 | linux-*-mips64el | linux-mips64el)
		target=linux64-mips64
		;;
	linux-microblaze* | linux-nios2* | linux-sh3 | linux-sh4 | linux-arc*)
		target=linux-generic32
		;;
	linux-powerpc)
		target=linux-ppc
		;;
	linux-powerpc64)
		target=linux-ppc64
		;;
	linux-powerpc64le)
		target=linux-ppc64le
		;;
	linux-riscv32)
		target=linux-generic32
		;;
	linux-riscv64)
		target=linux-generic64
		;;
	linux-sparc | linux-supersparc)
		target=linux-sparcv9
		;;
	esac

	useprefix=${prefix}
	if [ "x$useprefix" = "x" ]; then
		useprefix=/
	fi
	# WARNING: do not set compiler/linker flags (-I/-D etc.) in EXTRA_OECONF, as they will fully replace the
	# environment variables set by bitbake. Adjust the environment variables instead.
	HASHBANGPERL="/usr/bin/env perl" PERL=perl PERL5LIB="${S}/external/perl/Text-Template-1.46/lib/" \
	perl ${S}/Configure ${EXTRA_OECONF} ${PACKAGECONFIG_CONFARGS} --prefix=$useprefix --openssldir=${libdir}/ssl-1.1 --libdir=${libdir} $target
	perl ${B}/configdata.pm --dump
}

do_install () {
	# Normal install target also does install_docs, which we don't
	# want here.
	oe_runmake DESTDIR="${D}" install_sw install_ssldirs
	install -d ${D}${includedir}/openssl-1.1
	mv ${D}${includedir}/openssl ${D}${includedir}/openssl-1.1
	for pcf in openssl libssl libcrypto; do
	    mv ${D}${libdir}/pkgconfig/$pcf.pc ${D}${libdir}/pkgconfig/$pcf-1.1.pc
	    sed -i -e's,/include$,/include/openssl-1.1,' -e's,/${baselib}$,/${baselib}/openssl-1.1,' ${D}${libdir}/pkgconfig/$pcf-1.1.pc
	done
	oe_multilib_header openssl-1.1/openssl/opensslconf.h
	# Move .so symlinks and static libraries to a subdirectory
	install -d ${D}${libdir}/openssl-1.1
	for libname in crypto ssl; do
	    rm -f ${D}${libdir}/lib$libname.so
	    ln -s ../lib$libname.so.1.1 ${D}${libdir}/openssl-1.1/lib$libname.so
	    mv ${D}${libdir}/lib$libname.a ${D}${libdir}/openssl-1.1/
	done
	for binname in openssl c_rehash; do
	    mv ${D}${bindir}/$binname ${D}${bindir}/$binname-1.1
	done
}

do_install:append:class-native () {
	create_wrapper ${D}${bindir}/openssl-1.1 \
	    OPENSSL_CONF=${libdir}/ssl-1.1/openssl.cnf \
	    SSL_CERT_DIR=${libdir}/ssl-1.1/certs \
	    SSL_CERT_FILE=${libdir}/ssl-1.1/cert.pem \
	    OPENSSL_ENGINES=${libdir}/engines-1.1
}

do_install:append:class-nativesdk () {
	mkdir -p ${D}${SDKPATHNATIVE}/environment-setup.d
	install -m 644 ${WORKDIR}/environment.d-openssl.sh ${D}${SDKPATHNATIVE}/environment-setup.d/openssl.sh
	sed 's|/usr/lib/ssl/|/usr/lib/ssl-1.1/|g' -i ${D}${SDKPATHNATIVE}/environment-setup.d/openssl.sh
}

PTEST_BUILD_HOST_FILES += "configdata.pm"
PTEST_BUILD_HOST_PATTERN = "perl_version ="
do_install_ptest () {
	# Prune the build tree
	rm -f ${B}/fuzz/*.* ${B}/test/*.*

	cp ${S}/Configure ${B}/configdata.pm ${D}${PTEST_PATH}
	cp -r ${S}/external ${B}/test ${S}/test ${B}/fuzz ${S}/util ${B}/util ${D}${PTEST_PATH}

	# For test_shlibload
	ln -s ${libdir}/libcrypto.so.1.1 ${D}${PTEST_PATH}/
	ln -s ${libdir}/libssl.so.1.1 ${D}${PTEST_PATH}/

	install -d ${D}${PTEST_PATH}/apps
	ln -s ${bindir}/openssl ${D}${PTEST_PATH}/apps
	install -m644 ${S}/apps/*.pem ${S}/apps/*.srl ${S}/apps/openssl.cnf ${D}${PTEST_PATH}/apps
	install -m755 ${B}/apps/CA.pl ${D}${PTEST_PATH}/apps

	install -d ${D}${PTEST_PATH}/engines
	install -m755 ${B}/engines/ossltest.so ${D}${PTEST_PATH}/engines
}

# Add the openssl.cnf file to the openssl-conf package. Make the libcrypto
# package RRECOMMENDS on this package. This will enable the configuration
# file to be installed for both the openssl-bin package and the libcrypto
# package since the openssl-bin package depends on the libcrypto package.

PACKAGES =+ "libcrypto111 libssl111 ${BPN}-conf ${PN}-engines ${PN}-misc"

FILES:libcrypto111 = "${libdir}/libcrypto${SOLIBS}"
FILES:libssl111 = "${libdir}/libssl${SOLIBS}"
FILES:${BPN}-conf = "${libdir}/ssl-1.1/openssl.cnf*"
FILES:${PN}-engines = "${libdir}/engines-1.1"
FILES:${PN}-misc = "${libdir}/ssl-1.1/misc"
FILES:${PN} =+ "${libdir}/ssl-1.1/*"
FILES:${PN}-dev += "${libdir}/openssl-1.1/lib*${SOLIBSDEV}"
FILES:${PN}-staticdev += "${libdir}/openssl-1.1/lib*.a"
FILES:${PN}:append:class-nativesdk = " ${SDKPATHNATIVE}/environment-setup.d/${BPN}.sh"

CONFFILES:${BPN}-conf = "${sysconfdir}/ssl/openssl.cnf"

RRECOMMENDS:libcrypto111 += "${BPN}-conf"
RDEPENDS:${PN}-ptest += "${PN}-bin perl perl-modules bash"

RDEPENDS:${PN}-bin += "${BPN}-conf"

BBCLASSEXTEND = "native nativesdk"

CVE_PRODUCT = "openssl:openssl"

CVE_VERSION_SUFFIX = "alphabetical"

# Only affects OpenSSL >= 1.1.1 in combination with Apache < 2.4.37
# Apache in meta-webserver is already recent enough
CVE_CHECK_IGNORE += "CVE-2019-0190"
