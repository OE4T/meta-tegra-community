SUMMARY = "NVIDIA Holoscan SDK"
HOMEPAGE = "https://developer.nvidia.com/holoscan-sdk"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE.txt;md5=44b1a6d36426b67ca4b9e75c9d3b288b"

SRC_URI = " \
    git://github.com/nvidia-holoscan/holoscan-sdk.git;nobranch=1;protocol=https;tag=v${PV} \
    file://0001-Updates-for-OE-cross-builds.patch \
    file://0002-tests-Allow-installing-the-C-test-suites.patch \
    file://0003-examples-Resolve-holoviz-example-assets-in-an-instal.patch \
    file://0004-tests-Allow-installing-the-Python-suites-helpers.patch \
    file://0005-cmake-Give-cross-compiling-consumers-a-host-runnable-flatc.patch \
    file://0006-tests-Give-the-HoloMQ-retirement-stress-test-two-CUDA-queues.patch \
    file://0007-benchmark-Install-the-python_payload_conversion-modules.patch \
    file://0008-tests-Allow-installing-the-C-ctest-entries-programs.patch \
    file://0009-python-Build-the-testing-module-with-NOSTRIP.patch \
    file://0010-test-python-resolve-the-HoloMQ-probe-s-pinned-device.patch \
    file://0011-runtime-fall-back-when-a-causal-successor-defers.patch \
    file://oe-providers.cmake \
    file://gen-ctest-manifest.py \
    file://ctest-replay.py \
    file://run-ptest \
"
SRCREV = "358c772f2d2e88d117651a961d6f6bf9b650cf8d"

COMPATIBLE_MACHINE = "(cuda)"

inherit pkgconfig cmake cuda python3targetconfig ptest

DEPENDS += " \
    cpm-cmake cuda-cccl cuda-nvtx dlpack flatbuffers flatbuffers-native fmt \
    iceoryx2-cxx magic-enum matx rapids-cmake spdlog tl-expected yaml-cpp \
    yyjson \
"
DEPENDS += "${@bb.utils.contains('DISTRO_FEATURES', 'ptest', 'googletest reflect-cpp', '', d)}"

PACKAGECONFIG ??= " \
    inference vision python examples benchmarks testing \
    ${@bb.utils.contains('DISTRO_FEATURES', 'vulkan opengl', 'visualization', '', d)} \
"
PACKAGECONFIG[vision] = "-DHOLOSCAN_BUILD_VISION=ON,-DHOLOSCAN_BUILD_VISION=OFF,cvcuda libnpp"
PACKAGECONFIG[examples] = "-DHOLOSCAN_BUILD_EXAMPLES=ON,-DHOLOSCAN_BUILD_EXAMPLES=OFF"
PACKAGECONFIG[benchmarks] = "-DHOLOSCAN_BUILD_BENCHMARKS=ON,-DHOLOSCAN_BUILD_BENCHMARKS=OFF,googlebenchmark"
PACKAGECONFIG[inference] = "-DHOLOSCAN_BUILD_INFERENCE=ON,-DHOLOSCAN_BUILD_INFERENCE=OFF,tensorrt-core tensorrt-plugins"
PACKAGECONFIG[testing] = "-DHOLOSCAN_BUILD_TESTING_LIBRARY=ON,-DHOLOSCAN_BUILD_TESTING_LIBRARY=OFF"
HOLOSCAN_VISUALIZATION_CMAKE = " \
    -DHOLOSCAN_BUILD_VISUALIZATION=ON \
    -DHOLOVIZ_SLANGC=${STAGING_BINDIR_NATIVE}/slangc \
    -DCPM_imgui_SOURCE=${RECIPE_SYSROOT}/opt/nvidia/imgui \
    -DHOLOSCAN_OE_glfw_LIBRARY=${RECIPE_SYSROOT}${libdir}/libglfw.so \
    -DHOLOSCAN_OE_glfw_INCLUDE_DIR=${RECIPE_SYSROOT}${includedir} \
    -DHOLOSCAN_OE_stb_INCLUDE_DIR=${RECIPE_SYSROOT}${includedir}/stb \
"
HOLOSCAN_VISUALIZATION_DEPENDS = " \
    glfw-3.4 imgui shader-slang-native stb \
    vulkan-headers vulkan-memory-allocator vulkan-utility-libraries vulkan-volk \
"
# volk dlopens libvulkan, so no NEEDED entry pulls in the loader or the ICD.
PACKAGECONFIG[visualization] = "${HOLOSCAN_VISUALIZATION_CMAKE},-DHOLOSCAN_BUILD_VISUALIZATION=OFF,${HOLOSCAN_VISUALIZATION_DEPENDS},vulkan-loader tegra-libraries-vulkan"
PYTHON_SITEPACKAGES_DIR_NATIVE = "${STAGING_LIBDIR_NATIVE}/python${PYTHON_BASEVERSION}/site-packages"
NANOBIND_CMAKE_DIR = "${PYTHON_SITEPACKAGES_DIR_NATIVE}/nanobind/cmake"
HOLOSCAN_PYTHON_CMAKE = " \
    -DHOLOSCAN_BUILD_PYTHON=ON \
    -DPython3_EXECUTABLE=${PYTHON} \
    -DPython_EXECUTABLE=${PYTHON} \
    -Dnanobind_ROOT=${NANOBIND_CMAKE_DIR} \
"
PACKAGECONFIG[python] = "${HOLOSCAN_PYTHON_CMAKE},\
-DHOLOSCAN_BUILD_PYTHON=OFF,python3-nanobind-native"

EXTRA_OECMAKE = " \
    -DHOLOSCAN_INSTALL_EXAMPLE_SOURCE=OFF \
    -DHOLOSCAN_INSTALL_BENCHMARK_SOURCE=OFF \
    -DHOLOSCAN_DOWNLOAD_DATASETS=OFF \
    -DHOLOSCAN_BUILD_EXPERIMENTAL_PARAMS_WEB_TUNER=ON \
"

EXTRA_OECMAKE += " -DHOLOSCAN_BUILD_CAMERA=OFF"
EXTRA_OECMAKE += " \
    -DHOLOSCAN_BUILD_TESTS=${@bb.utils.contains('DISTRO_FEATURES', 'ptest', 'ON', 'OFF', d)} \
"
EXTRA_OECMAKE += " -DHOLOSCAN_BUILD_PYTHON_STUBS=OFF"
EXTRA_OECMAKE += " -DHOLOSCAN_VISION_BUNDLE_CVCUDA=OFF"
EXTRA_OECMAKE += " -DHOLOSCAN_USE_CUDA_TOOLKIT_NVTX=ON"
EXTRA_OECMAKE += " -DHOLOSCAN_INSTALL_DATA_DOWNLOAD_SCRIPTS=OFF"
EXTRA_OECMAKE += " \
    -DHOLOSCAN_USE_CACHE_DIR=OFF \
    -DHOLOSCAN_USE_SCCACHE=OFF \
"
EXTRA_OECMAKE += " \
    -DCMAKE_CXX_SCAN_FOR_MODULES=OFF \
"

EXTRA_OECMAKE += " \
    -DHOLOSCAN_INSTALL_TESTS_DIR=${@os.path.relpath(d.getVar('PTEST_PATH'), d.getVar('prefix'))} \
"

CPM_CACHE_DIR = "${WORKDIR}/cpm-cache"
EXTRA_OECMAKE += " \
    -DFETCHCONTENT_SOURCE_DIR_RAPIDS-CMAKE=${RECIPE_SYSROOT}/opt/nvidia/rapids-cmake \
    -DCPM_DOWNLOAD_LOCATION=${RECIPE_SYSROOT}${datadir}/cpm/CPM_0.42.0.cmake \
    -DCPM_SOURCE_CACHE=${CPM_CACHE_DIR} \
    -DCPM_USE_LOCAL_PACKAGES=ON \
"

EXTRA_OECMAKE += " \
    -DCCCL_DIR=${RECIPE_SYSROOT}${prefix}/local/cuda-${CUDA_VERSION}/lib/cmake/cccl \
"

EXTRA_OECMAKE += " \
    -DCMAKE_PROJECT_INCLUDE=${UNPACKDIR}/oe-providers.cmake \
    -DHOLOSCAN_OE_FLATBUFFERS_LIBRARY=${RECIPE_SYSROOT}${libdir}/libflatbuffers.so \
    -DHOLOSCAN_OE_FLATBUFFERS_INCLUDE_DIR=${RECIPE_SYSROOT}${includedir} \
    -DHOLOSCAN_OE_FLATC=${RECIPE_SYSROOT_NATIVE}${bindir}/flatc \
    -DHOLOSCAN_OE_yaml_cpp_LIBRARY=${RECIPE_SYSROOT}${libdir}/libyaml-cpp.so \
    -DHOLOSCAN_OE_yaml_cpp_INCLUDE_DIR=${RECIPE_SYSROOT}${includedir} \
    -DHOLOSCAN_OE_yyjson_LIBRARY=${RECIPE_SYSROOT}${libdir}/libyyjson.so \
    -DHOLOSCAN_OE_yyjson_INCLUDE_DIR=${RECIPE_SYSROOT}${includedir} \
    -DHOLOSCAN_OE_reflectcpp_LIBRARY=${RECIPE_SYSROOT}${libdir}/libreflectcpp.so \
    -DHOLOSCAN_OE_reflectcpp_INCLUDE_DIR=${RECIPE_SYSROOT}${includedir} \
"

do_configure:prepend() {
    install -d ${CPM_CACHE_DIR}
}

do_component_install() {
    bbnote ${CMAKE_VERBOSE} cmake --install '${B}' --prefix '${D}${prefix}' --component "$@"
    eval ${CMAKE_VERBOSE} cmake --install '${B}' --prefix '${D}${prefix}' --component "$@"
}

def holoscan_install_components(d):
    enabled = d.getVar("PACKAGECONFIG").split()
    modules = [m for m in ("vision", "visualization", "inference") if m in enabled]

    testing = "testing" in enabled

    components = ["holoscan-core", "holoscan-sensor-io", "holoscan-holomq"]
    components += ["holoscan-%s" % m for m in modules]
    if testing:
        components += ["holoscan-testing"]
    components += ["holoscan-config", "holoscan-config-io",
                   "holoscan-experimental-params-web-tuner", "holoscan-dependencies"]
    if "examples" in enabled:
        components += ["holoscan-core-examples"]
        components += ["holoscan-%s-examples" % m for m in modules]
        if testing:
            components += ["holoscan-testing-examples"]
    if "benchmarks" in enabled:
        components += ["holoscan-benchmarks"]
    if "python" in enabled:
        components += ["holoscan-python_libs", "holoscan-sensor-io-python_libs"]
        components += ["holoscan-%s-python_libs" % m for m in modules]
        if testing:
            components += ["holoscan-testing-python_libs"]
    return " ".join(components + ["holoscan-cpack"])

HOLOSCAN_COMPONENTS = "${@holoscan_install_components(d)}"
HOLOSCAN_CUDA_MAJOR = "${@d.getVar('CUDA_VERSION').split('.')[0]}"
# The CUDA runtime some ctests dlopen by path. The target has it by SONAME only,
# as the unversioned link is in cuda-cudart-dev.
HOLOSCAN_CUDART = "${prefix}/local/cuda-${CUDA_VERSION}/${baselib}/libcudart.so"

do_install() {
    for component in ${HOLOSCAN_COMPONENTS}; do
        do_component_install $component
    done

    # Has to come after holoscan-core for the CMake package configuration.
    do_component_install holoscan

    # Upstream's benchmark ctests drive most binaries through runner scripts
    # (*.py), and some through cmake -P assertions (*.cmake).
    # HOLOSCAN_INSTALL_BENCHMARK_SOURCE would install those along with every
    # benchmark's C++ source and CMakeLists, so only the scripts are installed,
    # in the same tree the binaries are.
    if ${@bb.utils.contains('PACKAGECONFIG', 'benchmarks', 'true', 'false', d)}; then
        (cd ${S}/benchmark && find . -type f \( -name '*.py' -o -name '*.sh' -o -name '*.cmake' \) \
             -not -path '*/__pycache__/*' -not -path './cmake/*') | while read -r script; do
            install -D -m 0644 ${S}/benchmark/$script ${D}${datadir}/benchmark/$script
        done
    fi

    # The Python examples and the Python drivers of C++ examples are scripts,
    # which upstream installs only with HOLOSCAN_INSTALL_EXAMPLE_SOURCE, along
    # with every C++ example's source. Install just them, beside the C++
    # binaries, where their ctests and test_shipped_examples look for them.
    if ${@bb.utils.contains('PACKAGECONFIG', 'python examples', 'true', 'false', d)}; then
        (cd ${S}/examples && find . -type f \( -name '*.py' -o -path '*/python/*.cmake' \) \
             -not -path '*/__pycache__/*') | while read -r script; do
            install -D -m 0644 ${S}/examples/$script ${D}${libdir}/holoscan/examples/$script
        done
    fi

    # Some C++ examples' ctests drive the binary through a cmake -P script beside
    # it, which upstream installs only with HOLOSCAN_INSTALL_EXAMPLE_SOURCE.
    if ${@bb.utils.contains('PACKAGECONFIG', 'examples', 'true', 'false', d)}; then
        (cd ${S}/examples && find . -path '*/cpp/*.cmake' -type f) | while read -r script; do
            install -D -m 0644 ${S}/examples/$script ${D}${libdir}/holoscan/examples/$script
        done
    fi

    # holoscan.__version__ comes from the metadata of the wheel, which
    # upstream names after the CUDA major version.
    if ${@bb.utils.contains('PACKAGECONFIG', 'python', 'true', 'false', d)}; then
        distinfo=${D}${PYTHON_SITEPACKAGES_DIR}/holoscan_core_cu${HOLOSCAN_CUDA_MAJOR}-${PV}.dist-info
        install -d $distinfo
        printf 'Metadata-Version: 2.1\nName: holoscan-core-cu%s\nVersion: %s\n' \
            ${HOLOSCAN_CUDA_MAJOR} ${PV} > $distinfo/METADATA
        echo oe > $distinfo/INSTALLER
    fi
}

do_install_ptest() {
    do_component_install holoscan-tests

    sed -i -e 's|@LIBDIR@|${libdir}|g' ${D}${PTEST_PATH}/run-ptest

    install -m 0755 ${UNPACKDIR}/ctest-replay.py ${D}${PTEST_PATH}/

    # The ctest drivers of the test trees: cmake -P scripts, Python checks and the
    # schemas and data they read, under source/ at their source-tree path.
    for tree in tests python/tests; do
        (cd ${S} && find $tree -type f \( -name '*.cmake' -o -name '*.py' -o -name '*.pyi' \
             -o -name '*.fbs' -o -name '*.json' -o -name '*.yaml' \) \
             -not -path '*/__pycache__/*') | while read -r file; do
            install -D -m 0644 ${S}/$file ${D}${PTEST_PATH}/source/$file
        done
    done

    # Where each suite run whole is built, so an entry that names it runs the
    # installed one.
    find ${B} -path '*/CMakeFiles' -prune -o -type f -perm -u+x -print >${WORKDIR}/built-programs
    suite_maps=""
    for suite in ${D}${PTEST_PATH}/*; do
        [ -f "$suite" ] && [ -x "$suite" ] || continue
        name=$(basename "$suite")
        case $name in
            run-ptest | *.py) continue ;;
        esac
        built=$(grep "/$name\$" ${WORKDIR}/built-programs || true)
        if [ $(echo "$built" | grep -c .) -ne 1 ]; then
            bbfatal "cannot tell which build-tree program ${PTEST_PATH}/$name is: '$built'"
        fi
        suite_maps="$suite_maps --map $built=${PTEST_PATH}/$name"
    done

    # The suites upstream registers with gtest_discover_tests(), as their PRE_TEST
    # discovery scripts give them to gtest_discover_tests_impl(): run-ptest lists
    # their cases on the target and runs each in a process of its own, as ctest
    # does. A suite started from the top of the build tree starts from the
    # suites' directory, which holds the build-root-relative files they read.
    # CMake 4.4 replaced the discovery script with a discover_tests() call in the
    # suite's _include.cmake, which a POST_BUILD suite's lacks.
    discoveries=""
    for discovery in $(cd ${B} && find . -path '*/CMakeFiles' -prune -o \
            \( -name '*_discovery.cmake' -o -name '*_include.cmake' \) -print | sort); do
        case $discovery in
            *_include.cmake)
                grep -q 'GoogleTest/DiscoverTests.cmake' ${B}/$discovery || continue ;;
        esac
        discoveries="$discoveries --discovery ${B}/$discovery"
    done
    ${PYTHON} ${UNPACKDIR}/gen-ctest-manifest.py $discoveries \
        --discovery-root ${PTEST_PATH} \
        --build-dir ${B} --source-dir ${S} \
        --dart-config ${B}/DartConfiguration.tcl \
        $suite_maps \
        --map ${B}/lib=${libdir} \
        --work-dir ${WORKDIR} --image ${D} \
        --output ${D}${PTEST_PATH}/gtest.manifest.json

    # The HoloMQ suites run through their ctest entries, which split them by
    # gtest filter and run the child programs standalone.
    ctest --test-dir ${B}/tests/holomq/unit --show-only=json-v1 >${WORKDIR}/holomq-tests.json
    ${PYTHON} ${UNPACKDIR}/gen-ctest-manifest.py \
        --tests ${WORKDIR}/holomq-tests.json \
        --build-dir ${B} --source-dir ${S} \
        --dart-config ${B}/DartConfiguration.tcl \
        --map ${B}/tests/holomq/unit=${PTEST_PATH}/holomq \
        --work-dir ${WORKDIR} --image ${D} \
        --output ${D}${PTEST_PATH}/holomq.manifest.json
    manifests="--exclude-manifest ${D}${PTEST_PATH}/holomq.manifest.json"

    if ${@bb.utils.contains('PACKAGECONFIG', 'benchmarks', 'true', 'false', d)}; then
        ctest --test-dir ${B}/benchmark --show-only=json-v1 >${WORKDIR}/benchmark-tests.json
        ${PYTHON} ${UNPACKDIR}/gen-ctest-manifest.py \
            --tests ${WORKDIR}/benchmark-tests.json \
            --build-dir ${B}/benchmark --source-dir ${S}/benchmark \
            --installed-dir ${datadir}/benchmark \
            --dart-config ${B}/benchmark/DartConfiguration.tcl \
            --map ${B}/python/lib=${PYTHON_SITEPACKAGES_DIR} \
            --map ${B}/lib=${PTEST_PATH}/helpers/lib \
            $suite_maps \
            --tool ${PYTHON}=${bindir}/python3 \
            --tool ${STAGING_BINDIR_NATIVE}/cmake=${bindir}/cmake \
            --work-dir ${WORKDIR} --image ${D} \
            --output ${D}${PTEST_PATH}/benchmarks.manifest.json
        manifests="$manifests --exclude-manifest ${D}${PTEST_PATH}/benchmarks.manifest.json"
    fi

    if ${@bb.utils.contains('PACKAGECONFIG', 'python examples', 'true', 'false', d)}; then
        holoscan_python_ptest
        manifests="$manifests --exclude-manifest ${D}${PTEST_PATH}/python.manifest.json"
    fi

    if ${@bb.utils.contains('PACKAGECONFIG', 'examples', 'true', 'false', d)}; then
        holoscan_examples_ptest
        manifests="$manifests --exclude-manifest ${D}${PTEST_PATH}/examples.manifest.json"
    fi

    # Every other ctest entry of the build tree: the explicit entries of the C++
    # test directories and of the modules, and whatever the manifests above do not
    # select. ctest would run gtest_discover_tests' discovery on the host for the
    # whole tree, so it reads a copy of the tree's CTestTestfile.cmake files
    # without the include()s that discovery hides behind; gtest.manifest.json
    # covers those suites. Left out are the entries run-ptest covers itself: each
    # holoviz suite, the HoloViz and HoloInfer examples it drives, and
    # rate_no_exceptions_test, which it runs whole as upstream does.
    mirror=${WORKDIR}/ctest-mirror
    rm -rf $mirror
    (cd ${B} && find . -path '*/CMakeFiles' -prune -o -name CTestTestfile.cmake -print) |
    while read -r ctestfile; do
        install -d $mirror/$(dirname $ctestfile)
        grep -v '^include(' ${B}/$ctestfile >$mirror/$ctestfile
    done
    ctest --test-dir $mirror --show-only=json-v1 >${WORKDIR}/all-tests.json
    ${PYTHON} ${UNPACKDIR}/gen-ctest-manifest.py \
        --tests ${WORKDIR}/all-tests.json $manifests --alias $mirror=${B} \
        --unreplayable "INSTALLED_CONSUMERS.external_expected=configures and builds the expected_installed_consumer project with CMake, which needs its sources, a compiler and a build tool" \
        --unreplayable "INSTALLED_CONSUMERS.dependency_layout=configures the flatbuffers_predeclared_provider project with CMake, which needs its sources, a compiler and a build tool" \
        --unreplayable "INSTALLED_CONSUMERS.preparation_contract=configures consumer projects with CMake's Unix Makefiles generator, which needs make and a compiler" \
        --exclude '^(holoviz_test_|holoviz_[a-z_]+_path_smoke$|HoloscanInferenceExample\.|OPERATOR\.RATE_NO_EXCEPTIONS$)' \
        --build-dir ${B} --source-dir ${S} \
        --dart-config ${B}/DartConfiguration.tcl \
        $suite_maps \
        --map ${B}/examples=${libdir}/holoscan/examples \
        --map ${S}/examples=${libdir}/holoscan/examples \
        --map ${B}/modules/visualization/examples/operators=${libdir}/holoscan/examples/holoviz/operators \
        --map ${S}/modules=${PTEST_PATH}/python/modules \
        --map ${S}/python/holoscan/_tools=${PYTHON_SITEPACKAGES_DIR}/holoscan/_tools \
        --map ${S}/benchmark=${datadir}/benchmark \
        --map ${B}/lib=${libdir} \
        --map ${B}=${PTEST_PATH}/helpers \
        --map ${S}=${PTEST_PATH}/source \
        --run-in ${B}/python/lib \
        --host-tools-dir ${HOSTTOOLS_DIR} \
        --tool ${PYTHON}=${bindir}/python3 \
        --tool ${STAGING_BINDIR_NATIVE}/cmake=${bindir}/cmake \
        --tool ${RECIPE_SYSROOT}${HOLOSCAN_CUDART}=${HOLOSCAN_CUDART}.${HOLOSCAN_CUDA_MAJOR} \
        --work-dir ${WORKDIR} --image ${D} \
        --output ${D}${PTEST_PATH}/tests.manifest.json
}

holoscan_examples_ptest() {
    ctest --test-dir ${B}/examples --show-only=json-v1 >${WORKDIR}/examples-tests.json
    tests="--tests ${WORKDIR}/examples-tests.json"
    examples=${libdir}/holoscan/examples
    maps="--map ${B}/examples=$examples --map ${S}/examples=$examples"

    # Not ${B}/modules/vision: ctest would run its gtest_discover_tests on the host.
    if ${@bb.utils.contains('PACKAGECONFIG', 'vision', 'true', 'false', d)}; then
        vision_examples=0
        for ctestfile in $(find ${B}/modules/vision/examples -name CTestTestfile.cmake | sort); do
            vision_examples=$(expr $vision_examples + 1)
            json=${WORKDIR}/vision-examples-tests-$vision_examples.json
            ctest --test-dir $(dirname $ctestfile) --show-only=json-v1 >$json
            tests="$tests --tests $json"
        done
        if [ $vision_examples -eq 0 ]; then
            bbfatal "no ctest definitions found under ${B}/modules/vision/examples"
        fi
        maps="$maps --map ${B}/modules/vision/examples=$examples \
            --map ${S}/modules/vision/examples=$examples \
            --run-in-tree ${B}/modules/vision/examples"
    fi

    # The examples run from their installed directory, which holds the
    # configuration files their build directory did. The Python examples are
    # in python.manifest.json. A program only the tests use is a helper.
    ${PYTHON} ${UNPACKDIR}/gen-ctest-manifest.py $tests $maps \
        --exclude '^(EXAMPLE_PYTHON_|python-)' \
        --build-dir ${B} --source-dir ${S} \
        --dart-config ${B}/DartConfiguration.tcl \
        --run-in-tree ${B}/examples \
        --map ${B}=${PTEST_PATH}/helpers \
        --host-tools-dir ${HOSTTOOLS_DIR} \
        --tool ${PYTHON}=${bindir}/python3 \
        --tool ${STAGING_BINDIR_NATIVE}/cmake=${bindir}/cmake \
        --work-dir ${WORKDIR} --image ${D} \
        --output ${D}${PTEST_PATH}/examples.manifest.json
}

holoscan_python_ptest() {
    helpers=${PTEST_PATH}/helpers
    cp -R --no-preserve=ownership ${B}/python/lib/tests ${D}$helpers/python/lib/
    find ${D}$helpers/python/lib/tests -name __pycache__ -prune -exec rm -rf {} +
    for module in vision inference visualization; do
        [ -d ${S}/modules/$module/python ] || continue
        # The Python tests also load the module's example scripts by source path.
        (cd ${S}/modules && find $module/python -type f \( -name '*.py' -o -name '*.pyi' -o -name py.typed \) \
             -not -path '*/__pycache__/*'
         if [ -d $module/examples ]; then find $module/examples -path '*/python/*.py' -type f; fi) |
        while read -r file; do
            install -D -m 0644 ${S}/modules/$file ${D}${PTEST_PATH}/python/modules/$file
        done
    done

    json=${WORKDIR}/python-ctest
    rm -rf $json
    install -d $json/core
    ctest --test-dir ${B}/examples --show-only=json-v1 >$json/examples.json
    grep -v '^include(' ${B}/python/tests/core/CTestTestfile.cmake >$json/core/CTestTestfile.cmake
    ctest --test-dir $json/core --show-only=json-v1 >$json/core.json
    tests="--tests $json/examples.json --tests $json/core.json"
    for module in vision inference visualization; do
        if [ -f ${B}/modules/$module/python/CTestTestfile.cmake ]; then
            ctest --test-dir ${B}/modules/$module/python --show-only=json-v1 >$json/$module.json
            tests="$tests --tests $json/$module.json"
        fi
    done

    # The installed-package tests stage a package from the build tree in a
    # fixture, then check it. On the target the image's own install is that
    # package, so they check it instead; a staging the image does not match,
    # such as a core-only package without Sensor I/O, stays unreplayable.
    staged=${B}/python/tests/core
    ${PYTHON} ${UNPACKDIR}/gen-ctest-manifest.py $tests --alias $json/core=${B}/python/tests/core \
        --include '^(EXAMPLE_PYTHON_|python-)' \
        --build-dir ${B} --source-dir ${S} \
        --dart-config ${B}/DartConfiguration.tcl \
        --map ${B}/examples=${libdir}/holoscan/examples \
        --map ${S}/examples=${libdir}/holoscan/examples \
        --map ${S}/modules=${PTEST_PATH}/python/modules \
        --map ${S}/python/holoscan/_tools=${PYTHON_SITEPACKAGES_DIR}/holoscan/_tools \
        --map $staged/installed-python=${prefix} \
        --map $staged/installed-python-testing=${prefix} \
        --provided-fixture python_core_installed_package \
        --provided-fixture python_testing_installed_package \
        --map ${B}/lib=${libdir} \
        --map ${B}=$helpers \
        --map ${S}=${PTEST_PATH}/source \
        --run-in ${B}/python/lib \
        --tool ${PYTHON}=${bindir}/python3 \
        --tool ${STAGING_BINDIR_NATIVE}/cmake=${bindir}/cmake \
        --work-dir ${WORKDIR} --image ${D} \
        --output ${D}${PTEST_PATH}/python.manifest.json
}

# libiceoryx2_ffi_c.so has no SONAME, so shlib scanning misses it.
RDEPENDS:${PN} += "iceoryx2-c"
# python3-core and python3-json for ctest-replay.py; iceoryx2-c for the HoloMQ suites;
# cmake for the ctest entries that run a cmake -P driver or cmake -E env.
RDEPENDS:${PN}-ptest += "bash cmake coreutils iceoryx2-c python3-core python3-json tegra-tools-jetson-clocks"
# For the HOLOVIZ_VALIDATION=1 upstream sets on every holoviz suite.
RDEPENDS:${PN}-ptest += "${@bb.utils.contains('PACKAGECONFIG', 'visualization', \
                                              'vulkan-validation-layers', '', d)}"
RDEPENDS:${PN}-ptest += "${@bb.utils.contains('PACKAGECONFIG', 'examples', \
                                              '${PN}-examples', '', d)}"
RDEPENDS:${PN}-ptest += "${@bb.utils.contains('PACKAGECONFIG', 'benchmarks', \
                                              '${PN}-benchmarks ${HOLOSCAN_BENCHMARK_PTEST_RDEPENDS}', '', d)}"
RDEPENDS:${PN}-ptest += "${@bb.utils.contains('PACKAGECONFIG', 'python examples', \
                                              '${HOLOSCAN_PYTHON_PTEST_RDEPENDS}', '', d)}"
# What the Python suites import beyond the bindings' own dependencies.
HOLOSCAN_PYTHON_PTEST_RDEPENDS = " \
    cmake python3-${PN} python3-pytest python3-numpy python3-torch python3-cupy \
    python3-asyncio python3-compression python3-crypt python3-ctypes python3-io \
    python3-json python3-netclient python3-pydoc python3-unittest \
"
HOLOSCAN_BENCHMARK_PTEST_RDEPENDS = " \
    cmake python3-core python3-io python3-json python3-pytest \
    ${@bb.utils.contains('PACKAGECONFIG', 'python', \
                         'python3-${PN} python3-numpy python3-torch python3-cupy', '', d)} \
"

PACKAGE_BEFORE_PN = "python3-${PN} ${PN}-examples ${PN}-benchmarks"
FILES:python3-${PN} = "${PYTHON_SITEPACKAGES_DIR}"
RDEPENDS:python3-${PN} += " \
    ${PN} python3-core python3-asyncio \
    python3-datetime python3-difflib python3-numpy \
"

SUMMARY:${PN}-examples = "Holoscan SDK example applications"
FILES:${PN}-examples = "${libdir}/holoscan/examples"
RDEPENDS:${PN}-examples += "${PN} iceoryx2-c tegra-tools-jetson-clocks"
RDEPENDS:${PN}-examples += "${@bb.utils.contains('PACKAGECONFIG', 'python', 'python3-${PN}', '', d)}"
# The examples' $ORIGIN/../../../../ RPATH resolves to ${libdir}.
INSANE_SKIP:${PN}-examples += "useless-rpaths"

SUMMARY:${PN}-benchmarks = "Holoscan SDK benchmarks"
FILES:${PN}-benchmarks = "${datadir}/benchmark"
RDEPENDS:${PN}-benchmarks += "${PN} iceoryx2-c tegra-tools-jetson-clocks"
RDEPENDS:${PN}-benchmarks += " \
    python3-core python3-crypt python3-datetime python3-io python3-json \
    python3-netclient python3-shell python3-statistics \
"

FILES:${PN} += "${datadir}/holoscan"

# find_dependency()s in the CMake package config, invisible to shlib scanning.
# FlatBuffers exports flatbuffers::flatbuffers as its static library, fmt exports
# fmt::fmt-c the same way, and
# FindCUDAToolkit declares CUDA::cuda_driver only when the libcuda stub is there.
RDEPENDS:${PN}-dev += " \
    cuda-cccl \
    cuda-cudart-dev \
    cuda-driver-dev \
    cuda-nvtx-dev \
    dlpack-dev \
    flatbuffers-dev \
    flatbuffers-staticdev \
    fmt-dev \
    fmt-staticdev \
    iceoryx2-c-dev \
    iceoryx2-cmake-modules-dev \
    iceoryx2-cxx-dev \
    magic-enum-dev \
    tl-expected-dev \
"
# holoscan::holoinfer_utils links CUDA::nvrtc.
RDEPENDS:${PN}-dev += "${@bb.utils.contains('PACKAGECONFIG', 'inference', 'cuda-nvrtc-dev', '', d)}"
