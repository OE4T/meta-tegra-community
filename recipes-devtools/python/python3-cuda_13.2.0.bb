SUMMARY = "CUDA Python Low-level Bindings"
HOMEPAGE = "https://nvidia.github.io/cuda-python"
LICENSE = "Apache-2.0 AND LicenseRef-Proprietary"
LIC_FILES_CHKSUM = " \
    file://LICENSE.md;md5=5028fedd819277227c4246007f1c0190 \
    file://cuda_core/LICENSE;md5=2ee41112a44fe7014dce33e26468ba93 \
    file://cuda_pathfinder/LICENSE;md5=2ee41112a44fe7014dce33e26468ba93 \
    file://cuda_python/LICENSE;md5=f96b9ded45a4d44d2db90c334b69fed6 \
    file://cuda_bindings/LICENSE;md5=f96b9ded45a4d44d2db90c334b69fed6 \
"

DEPENDS = "python3-pyclibrary-native python3-cython-native python3-versioneer-native python3-setuptools-scm-native cuda-profiler-api"

SRC_URI = "\
    git://github.com/NVIDIA/cuda-python.git;protocol=https;nobranch=1;tag=v${PV} \
    file://0001-OE-cross-build-fixups.patch \
"
SRCREV = "06e60656c0dc2cd110b7603167fded0fff544833"

COMPATIBLE_MACHINE = "(tegra)"

inherit cuda python_poetry_core

B = "${S}"

export CUDA_HOME = "${STAGING_DIR_HOST}/usr/local/cuda-${CUDA_VERSION}"
export CUDA_PYTHON_PARALLEL_LEVEL = "${@oe.utils.cpu_count()}"
CUDA_PYTHON_PARALLEL_LEVEL[vardepvalue] = "1"

# The cuda-python monorepo versions each sub-package independently via
# setuptools-scm using component-specific tags (cuda-pathfinder-vX.Y.Z,
# cuda-core-vX.Y.Z, ...). Only the umbrella v${PV} tag is fetched (shallow) and
# the applied patch dirties the worktree, so setuptools-scm cannot resolve the
# per-component versions and falls back to a bogus "0.1.dev1", which breaks the
# cuda-bindings 'cuda-pathfinder~=1.1' and cuda-core 'cuda-pathfinder>=1.4.1'
# build requirements. Pin each explicitly to the versions at this SRCREV.
export SETUPTOOLS_SCM_PRETEND_VERSION_FOR_CUDA_PATHFINDER = "1.4.2"
export SETUPTOOLS_SCM_PRETEND_VERSION_FOR_CUDA_BINDINGS = "${PV}"
export SETUPTOOLS_SCM_PRETEND_VERSION_FOR_CUDA_CORE = "0.7.0"
export SETUPTOOLS_SCM_PRETEND_VERSION_FOR_CUDA_PYTHON = "${PV}"

CXXFLAGS += "-I${RECIPE_SYSROOT}/usr/local/cuda-${CUDA_VERSION}/include"
CFLAGS += "-I${RECIPE_SYSROOT}/usr/local/cuda-${CUDA_VERSION}/include"

PEP517_BUILD_DEPS_DIR = "${B}/.pep517-build-deps"

do_compile () {
    # Build pure-Python / leaf packages first.
    pyproject-build --no-isolation --wheel --outdir ${PEP517_WHEEL_PATH} ${B}/cuda_pathfinder ${PEP517_BUILD_OPTS}
    pyproject-build --no-isolation --wheel --outdir ${PEP517_WHEEL_PATH} ${B}/cuda_bindings   ${PEP517_BUILD_OPTS}

    # cuda_core's PEP 517 backend dynamically requires 'cuda-bindings==13.*' and
    # cythonizes against its .pxd files. The freshly built wheels are aarch64, so
    # we can't 'pip install' them into the x86_64 native build interpreter; unpack
    # them onto PYTHONPATH instead. This exposes the .dist-info (for the dep check)
    # and the .pxd files (for Cython cimports).
    rm -rf ${PEP517_BUILD_DEPS_DIR}
    install -d ${PEP517_BUILD_DEPS_DIR}
    for whl in ${PEP517_WHEEL_PATH}/cuda_pathfinder-*.whl ${PEP517_WHEEL_PATH}/cuda_bindings-*.whl; do
        [ -f "$whl" ] || bbfatal "Expected wheel not found: $whl"
        nativepython3 -m zipfile -e "$whl" ${PEP517_BUILD_DEPS_DIR}
    done
    if [ -n "$PYTHONPATH" ]; then
        export PYTHONPATH="${PEP517_BUILD_DEPS_DIR}:$PYTHONPATH"
    else
        export PYTHONPATH="${PEP517_BUILD_DEPS_DIR}"
    fi

    pyproject-build --no-isolation --wheel --outdir ${PEP517_WHEEL_PATH} ${B}/cuda_core   ${PEP517_BUILD_OPTS}
    pyproject-build --no-isolation --wheel --outdir ${PEP517_WHEEL_PATH} ${B}/cuda_python ${PEP517_BUILD_OPTS}
}

do_install () {
    for whl in ${PEP517_WHEEL_PATH}/*.whl; do
        bbnote "Installing wheel: $whl"
        nativepython3 -m installer ${INSTALL_WHEEL_COMPILE_BYTECODE} \
            --interpreter "${USRBINPATH}/env ${PEP517_INSTALL_PYTHON}" \
            --prefix=${prefix} \
            --destdir=${D} \
            $whl
    done
    find ${D} -path *.dist-info/RECORD -delete
}

PACKAGES =+ "python3-cuda-bindings python3-cuda-core python3-cuda-pathfinder python3-cuda-python"
FILES:python3-cuda-bindings = "${PYTHON_SITEPACKAGES_DIR}/cuda/bindings ${PYTHON_SITEPACKAGES_DIR}/cuda_bindings-*"
FILES:python3-cuda-core = "${PYTHON_SITEPACKAGES_DIR}/cuda/core ${PYTHON_SITEPACKAGES_DIR}/cuda_core-*"
FILES:python3-cuda-pathfinder = "${PYTHON_SITEPACKAGES_DIR}/cuda/pathfinder ${PYTHON_SITEPACKAGES_DIR}/cuda_pathfinder-*"
FILES:python3-cuda-python = "${PYTHON_SITEPACKAGES_DIR}/cuda_python-*"

RDEPENDS:python3-cuda-bindings  += "python3-cuda-pathfinder cuda-nvrtc libnvjitlink libnvvm libnvfatbin libcufile"
RDEPENDS:python3-cuda-core      += "python3-cuda-bindings python3-cuda-pathfinder python3-numpy"
RDEPENDS:python3-cuda-python    += "python3-cuda-bindings python3-cuda-pathfinder"

ALLOW_EMPTY:${PN} = "1"
RDEPENDS:${PN} = "python3-cuda-bindings python3-cuda-core python3-cuda-pathfinder python3-cuda-python"

INSANE_SKIP:${PN} += "already-stripped"
INSANE_SKIP:${PN}-src += "buildpaths"
