DESCRIPTION = "A fast multi-producer, multi-consumer lock-free concurrent queue for C++11"
HOMEPAGE = "https://github.com/cameron314/concurrentqueue"
LICENSE = "BSD-2-Clause & BSL-1.0"
LIC_FILES_CHKSUM = "file://LICENSE.md;md5=3e3bd5d3367ca1cdb53ff11ab44322ea"

inherit cmake

SRC_URI = "git://github.com/cameron314/concurrentqueue.git;protocol=https;nobranch=1;tag=v${PV}"
SRCREV = "6dd38b8a1dbaa7863aa907045f32308a56a6ff5d"
