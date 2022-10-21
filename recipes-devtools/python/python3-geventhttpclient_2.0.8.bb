SUMMARY = "A high performance, concurrent http client library for python with gevent"
HOMEPAGE = "https://github.com/geventhttpclient/geventhttpclient"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE.txt;md5=c90481ab10d7e28d51bd403cbd83e963 \
                    file://llhttp/LICENSE-MIT;md5=f5e274d60596dd59be0a1d1b19af7978"

SRC_URI = "gitsm://github.com/geventhttpclient/geventhttpclient.git;protocol=https;branch=master"
SRCREV = "bbafdf35d9ca1061da27e5c6fd4733fe3b0ef0f7"

S = "${WORKDIR}/git"

inherit setuptools3

RDEPENDS_${PN} = "python3"
