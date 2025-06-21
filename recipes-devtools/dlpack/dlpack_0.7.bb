SUMMARY = "Common in-memory tensor structure"
HOMEPAGE = "https://github.com/dmlc/dlpack"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=f62d4e85ba68a1574b74d97ab8dea9ab"

SRC_URI = "git://github.com/dmlc/dlpack.git;protocol=https;branch=main"

SRCREV = "e2bdd3bee8cb6501558042633fa59144cc8b7f5f"

inherit cmake

PACKAGECONFIG = ""
PACKAGECONFIG[docs] = "-DBUILD_DOCS=ON,-DBUILD_DOCS=OFF"
PACKAGECONFIG[mock] = "-DBUILD_MOCK=ON,-DBUILD_MOCK=OFF"

ALLOW_EMPTY:${PN} = "1"
