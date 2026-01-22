SUMMARY = "NVIDIA Holoscan SDK"
HOMEPAGE = "https://developer.nvidia.com/holoscan-sdk"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE.txt;md5=3b83ef96387f14655fc854ddc3c6bd57"

SRC_URI = "git://github.com/nvidia-holoscan/holoscan-sdk.git;protocol=https;nobranch=1;tag=v${PV}"
SRCREV = "e7cd7d15c4fa39d4c57985992486bbf75d147f47"

SRC_URI += " \
    file://desktop-icons \
    file://0001-Fix-GXF-TypenameAsString-error.patch \
    file://0002-Use-external-library-dependencies.patch \
    file://0003-Build-python-libs-with-install-RPATH.patch \
    file://0004-Fix-TensorRT-include-interface.patch \
    file://0005-Disable-various-warnings-as-errors.patch \
    file://0006-Remove-GXF-python-modules-install.patch \
    file://0007-Updates-for-OE-cross-builds.patch \
"

HOLOSCAN_INSTALL_PATH = "/opt/nvidia/holoscan"

COMPATIBLE_MACHINE = "(cuda)"

inherit pkgconfig cmake cuda setuptools3

# Network is need to download the dataset
# TODO: create a seperate recipe for dataset and add runtime dependency
do_compile[network] = "1"

# Add extra build paths.
EXTRA_OECMAKE:append = " \
    -DCMAKE_INSTALL_PREFIX=${HOLOSCAN_INSTALL_PATH} \
    -DGXF_DIR=${RECIPE_SYSROOT}/opt/nvidia/gxf/lib/cmake/GXF \
    -DIMGUI_SOURCE_DIR=${RECIPE_SYSROOT}/opt/nvidia/imgui \
    -Dstb_INCLUDE_DIRS=${RECIPE_SYSROOT}${includedir}/stb \
    -DCPM_SOURCE_CACHE=${RECIPE_SYSROOT}${datadir} \
    -DRAPIDS_CMAKE_DIR=${RECIPE_SYSROOT}/opt/nvidia/rapids-cmake \
    -Dglfw3_DIR=${RECIPE_SYSROOT}${libdir}/cmake/glfw3 \
    -DPYTHON_EXECUTABLE=${HOSTTOOLS_DIR}/python3 \
    -DTENSORRT_ROOT=${RECIPE_SYSROOT}${includedir} \
"

# Disable unused Holoscan build steps and components.
EXTRA_OECMAKE:append = " \
    -DHOLOSCAN_INSTALL_EXAMPLE_SOURCE=OFF \
    -DHOLOSCAN_BUILD_TESTS=OFF \
    -DHOLOSCAN_BUILD_DOCS=OFF \
    -DHOLOSCAN_USE_CCACHE=OFF \
"

OECMAKE_CXX_FLAGS:append = " -Wno-error=cpp"

DEPENDS += " \
    glfw-3.4 \
    glslang-native \
    grpc \
    grpc-native \
    gxf-core \
    libcublas-native \
    libnpp-native \
    libxcursor \
    libxinerama \
    onnxruntime \
    patchelf-native \
    protobuf \
    protobuf-native \
    pytorch \
    tensorrt-core \
    tensorrt-plugins \
    torchvision \
    ucx \
    ucxx \
    v4l-utils \
    vulkan-headers \
    vulkan-loader \
    yaml-cpp-080 \
    curl-native \
    stb \
    imgui \
    fmt \
    spdlog \
    cuda-cccl \
    hwloc \
    cli11 \
    dlpack \
    tl-expected \
    magic-enum \
    nvtx \
    rmm \
    googletest \
    python3-pybind11 \
    cpm-cmake \
    rapids-cmake \
    libeigen \
    concurrentqueue \
    matx \
"

RDEPENDS:${PN} = " \
    bash \
    libnpp \
    libv4l \
    onnxruntime \
    python3-cloudpickle \
    python3-cupy \
    python3-numpy \
    tegra-libraries-multimedia \
    tegra-libraries-multimedia-utils \
    tegra-libraries-multimedia-v4l \
    tegra-libraries-vulkan \
"

RDEPENDS:${PN}-dev = " \
    bash \
"

# This explicit do_configure avoids conflicts between the cmake and setuptools3 classes.
do_configure() {
    cmake_do_configure
}

# This explicit do_compile avoids conflicts between the cmake and setuptools3 classes.
do_compile() {
    cmake_do_compile
}

do_component_install() {
    bbnote ${CMAKE_VERBOSE} cmake --install '${B}' --prefix '${D}${HOLOSCAN_INSTALL_PATH}' --component "$@"
    eval ${CMAKE_VERBOSE} cmake --install '${B}' --prefix '${D}${HOLOSCAN_INSTALL_PATH}' --component "$@"
}

do_install() {
    do_component_install holoscan-core
    do_component_install holoscan-gxf_extensions
    do_component_install holoscan-examples
    do_component_install holoscan-gxf_libs
    do_component_install holoscan-gxf_bins
    do_component_install holoscan-modules
    do_component_install holoscan-python_libs
    do_component_install holoscan-data

    # Note that the holoscan component currently needs to be installed after holoscan-core
    # in order for the CMake configuration files to be installed correctly.
    do_component_install holoscan

    # Create symlinks to the SDK libraries in the system lib directory.
    install -d ${D}${libdir}
    for i in $(find ${D}/${HOLOSCAN_INSTALL_PATH}/lib -name "*.so*" -printf "%P\n"); do
        ln -s ${HOLOSCAN_INSTALL_PATH}/lib/$i ${D}${libdir}
    done

    # Link to the python packages in the system site-packages directory.
    install -d ${D}${PYTHON_SITEPACKAGES_DIR}
    ln -s ${HOLOSCAN_INSTALL_PATH}/python/lib/holoscan ${D}${PYTHON_SITEPACKAGES_DIR}

    # Create a data directory symlink to fix some relative runtime path issues.
    ln -s ${HOLOSCAN_INSTALL_PATH}/data ${D}/opt/nvidia/data

    # Install desktop icons for the applications.
    install -d ${D}${datadir}/applications
    install -m 0644 ${WORKDIR}/sources/desktop-icons/*.desktop ${D}${datadir}/applications
    install -d ${D}${datadir}/pixmaps
    install -m 0644 ${WORKDIR}/sources/desktop-icons/*.png ${D}${datadir}/pixmaps
}

FILES:${PN} += " \
    ${HOLOSCAN_INSTALL_PATH} \
    /opt/nvidia/data \
    ${libdir} \
"

FILES:${PN}-dev += " \
    ${HOLOSCAN_INSTALL_PATH}/include \
    ${HOLOSCAN_INSTALL_PATH}/lib/cmake \
"

FILES:${PN}-staticdev += " \
    ${HOLOSCAN_INSTALL_PATH}/lib/*.a \
"

SYSROOT_DIRS = " \
    ${HOLOSCAN_INSTALL_PATH} \
"

# The GXF libraries (and symlinks) use the .so suffix but need to be installed
# in the non-dev package.
FILES_SOLIBSDEV = ""
INSANE_SKIP:${PN} += "file-rdeps buildpaths"
INSANE_SKIP:${PN}-dev += "buildpaths"
INSANE_SKIP:${PN}-staticdev += "buildpaths"

INSANE_SKIP:${PN} += "dev-so"

# Prebuilt binaries (e.g. 'gxe') are already stripped of debugging information.
INSANE_SKIP:${PN} += "already-stripped"

# Relative RPATHs are used within Holoscan (e.g. $ORIGIN/../lib) and are
# required, but they incorrectly trigger the "probably-redundant RPATH" checks.
INSANE_SKIP:${PN} += "useless-rpaths"
