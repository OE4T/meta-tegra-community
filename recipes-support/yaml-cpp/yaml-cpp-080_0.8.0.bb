require yaml-cpp-080.inc

# Shared library variant. deepstream (and other prebuilt consumers) link
# against libyaml-cpp.so.0.8 at runtime.
EXTRA_OECMAKE += "-DYAML_BUILD_SHARED_LIBS=ON"
