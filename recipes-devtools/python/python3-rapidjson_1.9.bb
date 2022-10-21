SUMMARY = "Python wrapper around rapidjson"
HOMEPAGE = "https://github.com/python-rapidjson/python-rapidjson"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=4daf3929156304df67003c33274a98bd \
                    file://rapidjson/license.txt;md5=ba04aa8f65de1396a7e59d1d746c2125 \
                    file://rapidjson/bin/jsonschema/LICENSE;md5=9d4de43111d33570c8fe49b4cb0e01af \
                    file://rapidjson/contrib/natvis/LICENSE;md5=ec259ab094c66e4776e1da8b023540e0 \
                    file://rapidjson/thirdparty/gtest/LICENSE;md5=cbbd27594afd089daa160d3a16dd515a \
                    file://rapidjson/thirdparty/gtest/googlemock/LICENSE;md5=cbbd27594afd089daa160d3a16dd515a \
                    file://rapidjson/thirdparty/gtest/googlemock/scripts/generator/LICENSE;md5=2c0b90db7465231447cf2dd2e8163333 \
                    file://rapidjson/thirdparty/gtest/googletest/LICENSE;md5=cbbd27594afd089daa160d3a16dd515a"

SRC_URI = "gitsm://github.com/python-rapidjson/python-rapidjson.git;protocol=https;branch=master"

SRCREV = "8f4ab8e197ca30c03726b675ae7cce6ac9d6622e"

S = "${WORKDIR}/git"

inherit setuptools3

RDEPENDS_${PN} += " \
    python3 \
    rapidjson \
"
