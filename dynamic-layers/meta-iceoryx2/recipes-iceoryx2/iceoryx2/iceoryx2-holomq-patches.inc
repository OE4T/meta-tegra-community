# The Holoscan SDK's HoloMQ transport needs three things from iceoryx2 that
# v0.9.3 does not have: a publisher sample-released handler, a per-recipient
# sample-replaced handler, and a blocking send that does not set up its wait
# state for a receiver that cannot drain. The SDK carries them as CPM
# PATCH_COMMANDs, which only run when it fetches and builds its own copy.
#
# They go on the ordinary recipes rather than into a private second chain
# because they are additive iceoryx2 features, not a Holoscan fork: the first
# two add API, the third changes one send path. They are imported unchanged
# from the SDK's cmake/deps/patches/iceoryx2 series; the rest of that series
# only adapts iceoryx2 to the SDK's vendored build and is not needed here.
#
# Every recipe unpacks the whole upstream tree and builds one subdirectory of it,
# so this goes on the four that make up the chain HoloMQ links against: iceoryx2
# (the Rust core and the FFI that iceoryx2.h is generated from), iceoryx2-c,
# iceoryx2-cxx (the C++ side of the two handlers) and iceoryx2-validation-suite,
# which builds the same cargo workspace as iceoryx2 and would otherwise ptest
# code the image does not ship. patchdir= is what lets one .inc serve them all:
# their S values differ, the hunks are all relative to the top of the tree.

FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI += " \
    file://0001-add-sample-released-handler.patch;patchdir=${UNPACKDIR}/${BP} \
    file://0002-add-sample-replaced-handler.patch;patchdir=${UNPACKDIR}/${BP} \
    file://0003-defer-blocking-send-wait-setup.patch;patchdir=${UNPACKDIR}/${BP} \
"
