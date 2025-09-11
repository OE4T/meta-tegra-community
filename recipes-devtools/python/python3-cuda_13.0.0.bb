SUMMARY = "CUDA Python Low-level Bindings"
HOMEPAGE = "https://nvidia.github.io/cuda-python"
LICENSE = "Apache-2.0 & Proprietary"
LIC_FILES_CHKSUM = " \
    file://LICENSE.md;md5=5108dd6a99a747a12651ee02e21902f2 \
    file://cuda_core/LICENSE;md5=2ee41112a44fe7014dce33e26468ba93 \
    file://cuda_pathfinder/LICENSE;md5=2ee41112a44fe7014dce33e26468ba93 \
    file://cuda_python/LICENSE;md5=06eb7fc760e442991e1d467aa9d5d993 \
    file://cuda_bindings/LICENSE;md5=06eb7fc760e442991e1d467aa9d5d993 \
"

DEPENDS = "python3-pyclibrary-native python3-cython-native python3-versioneer-native cuda-profiler-api unzip-native"

SRC_URI = " \
    git://github.com/NVIDIA/cuda-python.git;protocol=https;nobranch=1;tag=v${PV} \
    file://0001-OE-cross-build-fixups.patch \
"
SRCREV = "15e99e99ae607e023f37cc791a6650c5ae38c076"

COMPATIBLE_MACHINE = "(tegra)"

inherit cuda python_poetry_core

B = "${S}"

export CUDA_HOME = "${STAGING_DIR_HOST}/usr/local/cuda-${CUDA_VERSION}"
export PARALLEL_LEVEL = "${@oe.utils.cpu_count()}"
PARALLEL_VALUE[vardepvalue] = "1"
CXXFLAGS += "-I${RECIPE_SYSROOT}/usr/local/cuda-${CUDA_VERSION}/include"
CFLAGS += "-I${RECIPE_SYSROOT}/usr/local/cuda-${CUDA_VERSION}/include"

do_compile () {
    # build all the wheel packages
    pyproject-build --no-isolation --wheel --outdir ${PEP517_WHEEL_PATH} ${B}/cuda_bindings ${PEP517_BUILD_OPTS}
    pyproject-build --no-isolation --wheel --outdir ${PEP517_WHEEL_PATH} ${B}/cuda_core ${PEP517_BUILD_OPTS}
    pyproject-build --no-isolation --wheel --outdir ${PEP517_WHEEL_PATH} ${B}/cuda_pathfinder ${PEP517_BUILD_OPTS}
    pyproject-build --no-isolation --wheel --outdir ${PEP517_WHEEL_PATH} ${B}/cuda_python ${PEP517_BUILD_OPTS}
}

do_install () {
    install -d ${D}${PYTHON_SITEPACKAGES_DIR}
    for whl in ${PEP517_WHEEL_PATH}/*.whl; do
        bbnote "Installing wheel: $whl"
        unzip -q $whl -d ${D}${PYTHON_SITEPACKAGES_DIR}
    done
}

RDEPENDS:${PN} += "python3-cupy"

INSANE_SKIP:${PN} += "already-stripped"
