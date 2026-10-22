SUMMARY = "libevhtp, Triton Inference Server fork"
DESCRIPTION = "Triton Inference Server's fork of libevhtp, a libevent-based \
HTTP server library, as required by the Triton HTTP endpoint."
HOMEPAGE = "https://github.com/triton-inference-server/third_party"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=68e2a80f5f9020a66f4512962817cd66"

SRC_URI = " \
    git://github.com/triton-inference-server/third_party.git;protocol=https;branch=r26.05 \
    file://0001-libevhtp-Fall-back-to-pkg-config-to-find-libevent.patch \
    file://0002-libevhtp-Use-GNUInstallDirs-for-install-and-pkg-conf.patch \
"
SRCREV = "4d8c8cec032bf696312e2c8dcc2ee077c05f0175"

PV .= "+git"

S = "${UNPACKDIR}/${BP}/libevhtp/libevhtp"

DEPENDS = "libevent"

PACKAGECONFIG ??= "pthreads"
PACKAGECONFIG[openssl] = "-DEVHTP_DISABLE_SSL=OFF,-DEVHTP_DISABLE_SSL=ON,openssl"
PACKAGECONFIG[onig] = "-DEVHTP_DISABLE_REGEX=OFF,-DEVHTP_DISABLE_REGEX=ON,onig"
PACKAGECONFIG[pthreads] = "-DEVHTP_DISABLE_EVTHR=OFF,-DEVHTP_DISABLE_EVTHR=ON"
PACKAGECONFIG[tracing] = "-DEVHTP_TRITON_ENABLE_TRACING=ON,-DEVHTP_TRITON_ENABLE_TRACING=OFF"

inherit pkgconfig cmake

EXTRA_OECMAKE = "-DEVHTP_TRITON_ENABLE_HTTP_CONTIGUOUS=ON"

# The fork changes struct evhtp_request but installs the same files as
# upstream libevhtp 1.2.18, so it must not be co-installed with it.
RCONFLICTS:${PN}-dev = "libevhtp-dev"
RCONFLICTS:${PN}-staticdev = "libevhtp-staticdev"
