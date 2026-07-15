require yaml-cpp-080.inc

# Static library variant. holoscan-sdk links yaml-cpp statically, so it depends
# on this recipe and never pulls in the shared library. The archive is packaged
# in ${PN}-staticdev; the main package is empty.
EXTRA_OECMAKE += "-DYAML_BUILD_SHARED_LIBS=OFF"

ALLOW_EMPTY:${PN} = "1"
