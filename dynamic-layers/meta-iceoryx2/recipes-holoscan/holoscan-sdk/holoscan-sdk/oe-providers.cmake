# Dependency providers for the OpenEmbedded cross build.
#
# CMAKE_PROJECT_INCLUDE runs this at the end of every project() in the tree,
# modules/holoviz included, so the declarations have to happen exactly once. It
# is also late enough that the compilers are enabled, which declaring an imported
# SHARED library needs.
include_guard(GLOBAL)

# FlatBuffers has to be satisfied from two places at once: the runtime library
# from the target sysroot, flatc as a binary that runs on the build host.
# flatbuffers-dev exports flatbuffers::flatc pointing at the cross-built target
# binary, so find_package can only ever get one of the two right. Declaring both
# targets makes cmake/deps/flatbuffers.cmake take its "provider already present"
# path, where it only asks for the version read from the sysroot headers below.
foreach(_holoscan_oe_required
        HOLOSCAN_OE_FLATBUFFERS_LIBRARY
        HOLOSCAN_OE_FLATBUFFERS_INCLUDE_DIR
        HOLOSCAN_OE_FLATC)
    if(NOT ${_holoscan_oe_required})
        message(FATAL_ERROR "${_holoscan_oe_required} was not passed to the configure step")
    endif()
endforeach()
unset(_holoscan_oe_required)

add_library(flatbuffers::flatbuffers SHARED IMPORTED GLOBAL)
set_target_properties(flatbuffers::flatbuffers PROPERTIES
    IMPORTED_LOCATION "${HOLOSCAN_OE_FLATBUFFERS_LIBRARY}"
    INTERFACE_INCLUDE_DIRECTORIES "${HOLOSCAN_OE_FLATBUFFERS_INCLUDE_DIR}"
)

add_executable(flatbuffers::flatc IMPORTED GLOBAL)
set_target_properties(flatbuffers::flatc PROPERTIES
    IMPORTED_LOCATION "${HOLOSCAN_OE_FLATC}"
)

# The version generated headers static_assert against, taken from the sysroot rather than pinned.
file(STRINGS "${HOLOSCAN_OE_FLATBUFFERS_INCLUDE_DIR}/flatbuffers/base.h" _holoscan_oe_fb_version
    REGEX "^#define FLATBUFFERS_VERSION_(MAJOR|MINOR|REVISION) [0-9]+$")
string(REGEX REPLACE "[^;]*MAJOR ([0-9]+);[^;]*MINOR ([0-9]+);[^;]*REVISION ([0-9]+)" "\\1.\\2.\\3"
    Flatbuffers_VERSION "${_holoscan_oe_fb_version}")
unset(_holoscan_oe_fb_version)
if(NOT Flatbuffers_VERSION MATCHES "^[0-9]+\\.[0-9]+\\.[0-9]+$")
    message(FATAL_ERROR "Cannot read the FlatBuffers version from ${HOLOSCAN_OE_FLATBUFFERS_INCLUDE_DIR}/flatbuffers/base.h")
endif()

# schemas/CMakeLists.txt adds $<BUILD_INTERFACE:${Flatbuffers_SOURCE_DIR}/include>
# to holoscan_schema, which would otherwise expand to a bare "/include".
get_filename_component(Flatbuffers_SOURCE_DIR "${HOLOSCAN_OE_FLATBUFFERS_INCLUDE_DIR}" DIRECTORY)

# The parser backends behind config_io, params_io and the reflect-cpp differential
# test. Each has an ordinary CMake package in the sysroot, but the dependency modules
# run inside holoscan_add_dependency(), and a target a find_package() creates in there
# is gone by the time CMakeLists.txt links config_io. Declaring them here keeps them
# GLOBAL. None carries transitive dependencies as their recipes build them.
foreach(_holoscan_oe_backend yaml_cpp yyjson reflectcpp)
    foreach(_holoscan_oe_field LIBRARY INCLUDE_DIR)
        if(NOT HOLOSCAN_OE_${_holoscan_oe_backend}_${_holoscan_oe_field})
            message(FATAL_ERROR
                "HOLOSCAN_OE_${_holoscan_oe_backend}_${_holoscan_oe_field} was not passed to "
                "the configure step")
        endif()
    endforeach()
endforeach()
unset(_holoscan_oe_backend)
unset(_holoscan_oe_field)

add_library(yaml-cpp::yaml-cpp SHARED IMPORTED GLOBAL)
set_target_properties(yaml-cpp::yaml-cpp PROPERTIES
    IMPORTED_LOCATION "${HOLOSCAN_OE_yaml_cpp_LIBRARY}"
    INTERFACE_INCLUDE_DIRECTORIES "${HOLOSCAN_OE_yaml_cpp_INCLUDE_DIR}"
)

add_library(yyjson::yyjson SHARED IMPORTED GLOBAL)
set_target_properties(yyjson::yyjson PROPERTIES
    IMPORTED_LOCATION "${HOLOSCAN_OE_yyjson_LIBRARY}"
    INTERFACE_INCLUDE_DIRECTORIES "${HOLOSCAN_OE_yyjson_INCLUDE_DIR}"
)

# RFL_BUILD_SHARED has to match the library the recipe builds. reflect-cpp's own
# package configuration sets that define, so a hand-declared target has to as well.
add_library(reflectcpp::reflectcpp SHARED IMPORTED GLOBAL)
set_target_properties(reflectcpp::reflectcpp PROPERTIES
    IMPORTED_LOCATION "${HOLOSCAN_OE_reflectcpp_LIBRARY}"
    INTERFACE_INCLUDE_DIRECTORIES "${HOLOSCAN_OE_reflectcpp_INCLUDE_DIR}"
    INTERFACE_COMPILE_DEFINITIONS "RFL_BUILD_SHARED"
)

# cmake/deps/glfw.cmake asks for a package named "glfw", but GLFW installs itself as
# "glfw3", so find_package can never match and CPM always fetches. Declare the bare
# "glfw" target that glfwTargets.cmake itself exports.
#
# glfw and stb belong to modules/visualization, so the recipe passes them only with
# that PACKAGECONFIG. Declare them when it did and say nothing when it did not: a
# build without the module never reaches cmake/deps/glfw.cmake.
if(HOLOSCAN_OE_glfw_LIBRARY OR HOLOSCAN_OE_glfw_INCLUDE_DIR)
    foreach(_holoscan_oe_field LIBRARY INCLUDE_DIR)
        if(NOT HOLOSCAN_OE_glfw_${_holoscan_oe_field})
            message(FATAL_ERROR
                "HOLOSCAN_OE_glfw_${_holoscan_oe_field} was not passed to the configure step")
        endif()
    endforeach()
    unset(_holoscan_oe_field)

    add_library(glfw SHARED IMPORTED GLOBAL)
    set_target_properties(glfw PROPERTIES
        IMPORTED_LOCATION "${HOLOSCAN_OE_glfw_LIBRARY}"
        INTERFACE_INCLUDE_DIRECTORIES "${HOLOSCAN_OE_glfw_INCLUDE_DIR}"
    )
endif()

# cmake/deps/stb.cmake is download-only - stb has no CMake package - and expects
# stb_image.h with no subdirectory prefix, since it hands out the checkout root. The
# stb recipe's ${includedir}/stb stands in for that root. Visualization-only, as glfw
# above.
if(HOLOSCAN_OE_stb_INCLUDE_DIR)
    add_library(stb::stb INTERFACE IMPORTED GLOBAL)
    set_target_properties(stb::stb PROPERTIES
        INTERFACE_INCLUDE_DIRECTORIES "${HOLOSCAN_OE_stb_INCLUDE_DIR}"
    )
endif()
