SUMMARY = "RTLA (Real-Time Linux Analysis) tool"
DESCRIPTION = "RTLA is a meta-tool including commands to analyze real-time properties of Linux using kernel tracing."
HOMEPAGE = "https://www.kernel.org/doc/html/latest/tools/rtla/rtla.html"
LICENSE = "GPL-2.0-only"

inherit linux-kernel-base kernel-arch manpages
inherit python3-dir

S = "${UNPACKDIR}/${BP}"

DEPENDS = "\
    ${MLPREFIX}binutils \
    ${MLPREFIX}elfutils \
    bison-native \
    flex-native \
    libgcc \
    libtraceevent \
    libtracefs \
    python3-docutils-native \
    virtual/${MLPREFIX}libc \
    xz \
"

PROVIDES = "virtual/rtla"

RTLA_SRC ?= "\
    arch/${ARCH}/Makefile \
    arch/arm64/tools \
    Documentation/tools/rtla \
    Makefile \
    scripts/ \
    tools/arch \
    tools/build \
    tools/include \
    tools/lib \
    tools/Makefile \
    tools/scripts \
    tools/tracing/Makefile \
    tools/tracing/rtla \
"

deltask do_fetch
deltask do_unpack
do_patch[depends] += "virtual/kernel:do_shared_workdir"
do_patch[noexec] = "1"
do_package[depends] += "virtual/kernel:do_populate_sysroot"

do_copy_rtla_source_from_kernel[dirs] = "${S}"
do_copy_rtla_source_from_kernel[cleandirs] = "${S}"
addtask copy_rtla_source_from_kernel before do_configure after do_patch

python do_copy_rtla_source_from_kernel() {
    sources = (d.getVar("RTLA_SRC") or "").split()
    src_dir = d.getVar("STAGING_KERNEL_DIR")
    dest_dir = d.getVar("S")
    bb.utils.mkdirhier(dest_dir)
    bb.utils.prunedir(dest_dir)
    for s in sources:
        src = oe.path.join(src_dir, s)
        dest = oe.path.join(dest_dir, s)
        if not os.path.exists(src):
            bb.warn("Path does not exist: %s. Maybe RTLA_SRC lists more files than what your kernel version provides and needs." % src)
            continue
        if os.path.isdir(src):
            oe.path.copyhardlinktree(src, dest)
        else:
            src_path = os.path.dirname(s)
            os.makedirs(os.path.join(dest_dir, src_path), exist_ok=True)
            bb.utils.copyfile(src, dest)
}


EXTRA_OEMAKE = '\
    V=1 \
    VF=1 \
    -C ${S}/tools/tracing/rtla \
    O=${B} \
    CROSS_COMPILE=${TARGET_PREFIX} \
    ARCH=${ARCH} \
    CC="${CC}" \
    CCLD="${CC}" \
    LDSHARED="${CC} -shared" \
    AR="${AR}" \
    STRIP="${STRIP}" \
    LD="${LD}" \
    EXTRA_CFLAGS="-ldw -I${S}" \
    YFLAGS="-y --file-prefix-map=${WORKDIR}=${TARGET_DBGSRC_DIR}" \
    PKG_CONFIG=pkg-config \
    TMPDIR="${B}" \
    LIBUNWIND_DIR=${STAGING_EXECPREFIXDIR} \
'

EXTRA_OEMAKE += '\
    DESTDIR=${D} \
    prefix=${prefix} \
    bindir=${bindir} \
    sharedir=${datadir} \
    sysconfdir=${sysconfdir} \
    sharedir=${@os.path.relpath(datadir, prefix)} \
    mandir=${@os.path.relpath(mandir, prefix)} \
    infodir=${@os.path.relpath(infodir, prefix)} \
'

EXTRA_OEMAKE:append:task-configure = " JOBS=1"

do_configure() {
}

do_compile() {
    unset CFLAGS
    oe_runmake all
}

do_install() {
    unset CFLAGS
    oe_runmake install
}

INSANE_SKIP:${PN} += "already-stripped ldflags"
