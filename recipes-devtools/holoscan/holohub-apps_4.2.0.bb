SUMMARY = "NVIDIA HoloHub Applications"
HOMEPAGE = "https://github.com/nvidia-holoscan/holohub"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=86d3f3a95c324c9479bd8986968f4327"

SRC_URI = "\
    git://github.com/nvidia-holoscan/holohub.git;protocol=https;nobranch=1;tag=holoscan-sdk-${PV} \
    git://github.com/NIFTI-Imaging/nifti_clib.git;protocol=https;nobranch=1;tag=v3.0.0;destsuffix=nifti_clib;name=niftiClib \
    https://api.ngc.nvidia.com/v2/resources/nvidia/clara-holoscan/holoscan_colonoscopy_sample_data/versions/20230222/zip;downloadfilename=holoscan_colonoscopy_sample_data_20230222.zip;name=colonoscopy;subdir=${S}/data/colonoscopy_segmentation \
    https://api.ngc.nvidia.com/v2/resources/nvidia/clara-holoscan/endoscopy_out_of_body_detection/versions/20230127/zip;downloadfilename=endoscopy_out_of_body_detection_20230127.zip;name=endoscopy;subdir=${S}/data/endoscopy_out_of_body_detection \
    https://api.ngc.nvidia.com/v2/resources/nvidia/clara-holoscan/holoscan_endoscopy_sample_data/versions/20230222/zip;downloadfilename=holoscan_endoscopy_sample_data_20230222.zip;name=hendoscopy;subdir=${S}/data/endoscopy \
    https://api.ngc.nvidia.com/v2/resources/nvidia/clara-holoscan/ssd_surgical_tool_detection_model/versions/v0.1/zip;downloadfilename=ssd_surgical_tool_detection_model_v0.1.zip;name=ssd;subdir=${S}/data/ssd_model \
    https://api.ngc.nvidia.com/v2/resources/nvidia/clara-holoscan/monai_endoscopic_tool_segmentation_model/versions/v0.1/zip;downloadfilename=monai_endoscopic_tool_segmentation_model_v0.1.zip;name=monai;subdir=${S}/data/monai_tool_seg_model \
    https://api.ngc.nvidia.com/v2/resources/nvidia/clara-holoscan/holoscan_multi_ai_ultrasound_sample_data/versions/20230222/zip;downloadfilename=holoscan_multi_ai_ultrasound_sample_data_20230222.zip;name=multiai;subdir=${S}/data/multiai_ultrasound \
    https://api.ngc.nvidia.com/v2/resources/nvidia/clara-holoscan/holoscan_ultrasound_sample_data/versions/20240801/zip;downloadfilename=holoscan_ultrasound_sample_data_20240801.zip;name=ultrasound;subdir=${S}/data/ultrasound_segmentation \
    https://api.ngc.nvidia.com/v2/resources/nvidia/clara-holoscan/holoscan_volume_rendering_sample_data/versions/20230628/zip;downloadfilename=holoscan_volume_rendering_sample_data_20230628.zip;name=rendering;subdir=${S}/data/volume_rendering \
"
SRCREV = "43cbe6f2c913f7ea926ec8c25a0bda3c8cdb00b9"
SRCREV_niftiClib = "ff1371b24a859dbf8a3eaaed5b82dc4bc096a9b9"
SRC_URI[colonoscopy.sha256sum] = "0334d292414d19e9700a1d9cf5cfc9464215b7fa961b0fe74c425dae30a036d6"
SRC_URI[endoscopy.sha256sum] = "55f59e12f6cb9738d21e0cc1dea85d112d4278a02deeb234afbae9544af3afa3"
SRC_URI[hendoscopy.sha256sum] = "768028e770390838e0e750c13e6611e1dc73a88c278039b1464e23586127f627"
SRC_URI[ssd.sha256sum] = "cb5ba701a02e6c571e5174ffb75245f1e649c19b62ab19f42bf02747a2bb74bc"
SRC_URI[monai.sha256sum] = "b84bdc8a2539ea51ed6a368abdcb71006bc0b4a7b8f2bad3a99fc195921a1c03"
SRC_URI[multiai.sha256sum] = "5b9cc2dfa4e3c3c4fa7686aec564ce2e67e44ccb7e6a911ceef11b0271649ff9"
SRC_URI[ultrasound.sha256sum] = "d77859be57f96bfd93dcd406a811192618fc19b7b61a079aedd3a7b281fa9d5c"
SRC_URI[rendering.sha256sum] = "0e11f8f87e9041184821a9c4f7e1806fdc28d828ee14c69c4bbfcb050de5d8ff"

SRCREV_FORMAT = "holohub_niftiClib"

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
    -DHOLOHUB_DOWNLOAD_DATASETS=OFF \
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
    -DFETCHCONTENT_SOURCE_DIR_NIFTI_CLIB=${UNPACKDIR}/nifti_clib \
"

EXTRA_OECMAKE:append = " -DSPDLOG_FMT_EXTERNAL=ON"
CXXFLAGS:append = " -DSPDLOG_FMT_EXTERNAL"

# Generate GXF entities (.gxf_entities / .gxf_index) from the videos staged
# via SRC_URI.
do_compile:prepend() {
    for spec in \
        "colonoscopy_segmentation 720 576 3 30" \
        "endoscopy_out_of_body_detection 256 256 3 30" \
        "endoscopy 854 480 3 30" \
        "multiai_ultrasound 320 320 3 30" \
        "ultrasound_segmentation 256 256 3 30"
    do
        set -- ${spec}
        subdir=$1; width=$2; height=$3; channels=$4; framerate=$5
        data_dir="${S}/data/${subdir}"

        if [ ! -d "${data_dir}" ]; then
            bbwarn "Dataset directory ${data_dir} not found; skipping GXF entity generation"
            continue
        fi

        for ext in mp4 raw mpeg avi 264; do
            for video in "${data_dir}"/*.${ext}; do
                [ -f "${video}" ] || continue
                base=$(basename "${video}" ".${ext}")
                bbnote "Converting ${video} -> GXF entities (${width}x${height}@${framerate}fps)"
                ffmpeg -loglevel quiet -i "${video}" -pix_fmt rgb24 -f rawvideo pipe:1 | \
                    python3 ${S}/utilities/convert_video_to_gxf_entities.py \
                        --directory "${data_dir}" \
                        --width ${width} \
                        --height ${height} \
                        --channels ${channels} \
                        --framerate ${framerate} \
                        --basename "${base}"
            done
        done
    done
}

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
    if [ -d ${S}/data ]; then
        cp -rd --no-preserve=ownership ${S}/data ${D}${HOLOHUB_INSTALL_PATH}
    fi

    install -d ${D}${libdir}/cmake/holohub
    install -m 0644 ${S}/cmake/HoloHubConfigHelpers.cmake ${D}${libdir}/cmake/holohub/

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
INSANE_SKIP:${PN}-staticdev += "buildpaths"
