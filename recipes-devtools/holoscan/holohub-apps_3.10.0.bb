SUMMARY = "NVIDIA HoloHub Applications"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=86d3f3a95c324c9479bd8986968f4327"

SRC_URI = "git://github.com/nvidia-holoscan/holohub.git;protocol=https;nobranch=1;tag=holoscan-sdk-${PV}"
SRCREV = "e3edcf4c2b41901887630c803e66410d8c68c0cd"

SRC_URI += " \
    file://desktop-icons \
    file://0001-Add-install-rules.patch \
    file://0002-Remove-relative-gxf_extension-paths.patch \
    file://0003-Build-python-libs-with-install-RPATH-and-add-find-py.patch \
    file://0004-Enable-Emergent-apps.patch \
    file://0005-Fix-default-data-paths-in-python-apps.patch \
    file://0006-Fix-volume_renderer-application.patch \
    file://0007-Skip-model-download-for-object_detection_torch.patch \
    file://0008-Remove-native-CUDA_ARCHITECTURE.patch \
    file://0009-Updates-for-OE-cross-builds.patch \
"

HOLOHUB_INSTALL_PATH = "/opt/nvidia/holohub"

COMPATIBLE_MACHINE = "(cuda)"

inherit pkgconfig cmake cuda setuptools3-base

EXTRA_OECMAKE:append = " \
    -DBUILD_SAMPLE_APPS=ON \
    -DHOLOHUB_DOWNLOAD_DATASETS=ON \
    -DCMAKE_INSTALL_PREFIX=${HOLOHUB_INSTALL_PATH} \
    -Dholoscan_DIR=${RECIPE_SYSROOT}/opt/nvidia/holoscan/lib/cmake/holoscan \
    -DOP_aja_source=OFF \
    -DAPP_aja_video_capture=OFF \
    -DAPP_colonoscopy_segmentation=ON \
    -DAPP_endoscopy_out_of_body_detection=ON \
    -DAPP_endoscopy_tool_tracking=ON \
    -DAPP_multiai_endoscopy=ON \
    -DAPP_multiai_ultrasound=ON \
    -DAPP_ultrasound_segmentation=ON \
    -DAPP_volume_rendering=ON \
    -DSLANG_RUNTIME_TARGETS=${HOST_ARCH} \
"

# Allow CMake to fetch datasets or dependencies during the configure and compile steps.
do_configure[network] = "1"
do_compile[network] = "1"
EXTRA_OECMAKE:append = " \
    -DFETCHCONTENT_FULLY_DISCONNECTED=OFF \
"

EXTRA_OECMAKE:append = " -DSPDLOG_FMT_EXTERNAL=ON"
CXXFLAGS:append = " -DSPDLOG_FMT_EXTERNAL"

do_install:append() {
    # Create symlinks to the libraries in the system lib directory.
    install -d ${D}${libdir}
    for i in $(find ${D}/${HOLOHUB_INSTALL_PATH}/lib -name "*.so*" -printf "%P\n"); do
        ln -s ${HOLOHUB_INSTALL_PATH}/lib/$i ${D}${libdir}
    done

    # Link to the python packages in the system site-packages directory.
    install -d ${D}${PYTHON_SITEPACKAGES_DIR}
    ln -s ${HOLOHUB_INSTALL_PATH}/python/lib/holohub ${D}${PYTHON_SITEPACKAGES_DIR}

    # Install the datasets.
    if [ -d ${B}/data ]; then
        cp -rd --no-preserve=ownership ${B}/data ${D}${HOLOHUB_INSTALL_PATH}
    fi

    # Install desktop icons for the applications.
    install -d ${D}${datadir}/applications
    install -m 0644 ${UNPACKDIR}/desktop-icons/*.desktop ${D}${datadir}/applications
    install -d ${D}${datadir}/pixmaps
    install -m 0644 ${UNPACKDIR}/desktop-icons/*.png ${D}${datadir}/pixmaps
}

DEPENDS += " \
    claraviz \
    ffmpeg-native \
    holoscan-sdk \
    libcublas-native \
    libnpp-native \
    ngc-cli-native \
    python3-numpy-native \
    tensorrt-core \
    cuda-nvrtc-native \
    nlohmann-json \
    python3-pybind11 \
    libeigen \
    ucxx \
    rmm \
"

RDEPENDS:${PN} += " \
    python3-packaging \
    pytorch \
    torchvision \
    rxvt-unicode \
"

FILES:${PN}-staticdev += " \
    ${HOLOHUB_INSTALL_PATH}/lib/*.a \
"

FILES:${PN} += " \
    ${HOLOHUB_INSTALL_PATH} \
    ${libdir} \
"

# The GXF libraries (and symlinks) use the .so suffix but need to be installed
# in the non-dev package.
FILES_SOLIBSDEV = ""
INSANE_SKIP:${PN} += "dev-so"

# Relative RPATHs are used within HoloHub (e.g. $ORIGIN/../lib) and are
# required, but they incorrectly trigger the "probably-redundant RPATH" checks.
INSANE_SKIP:${PN} += "useless-rpaths"

# The pybind-generated libraries are stripped by default.
INSANE_SKIP:${PN} += "already-stripped"

INSANE_SKIP:${PN} += "buildpaths"
INSANE_SKIP:${PN}-dbg += "buildpaths"
