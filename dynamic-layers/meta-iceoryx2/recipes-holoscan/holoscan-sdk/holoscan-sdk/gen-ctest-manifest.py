#!/usr/bin/env python3
# SPDX-FileCopyrightText: Copyright (c) 2026 NVIDIA CORPORATION & AFFILIATES.
# SPDX-License-Identifier: MIT
#
# Translate the test definitions ctest reports for a build tree into a JSON
# manifest that ctest-replay.py runs on the target, rewriting every build-tree,
# source-tree and host-tool path to its installed counterpart.
#
# Every argument is classified, none is guessed at:
#   - a path under a --map or --tool prefix is rewritten to its target;
#   - a build-tree or source-tree path the image installs under --installed-dir
#     is rewritten to it;
#   - a build-tree path that does not exist in the build tree either is an output
#     the test writes, and moves under a per-test scratch tree on the target;
#   - a command run from --host-tools-dir is named for the target's PATH;
#   - anything else that points into the build host makes the test unreplayable,
#     and it is listed with the reason.
# Several --map prefixes may cover one path, a specific one and a general one;
# they are tried longest first, and the first the image installs wins.
# The same applies to the value of a NAME=value or -DNAME=value argument, which
# is how cmake -P drivers and cmake -E env receive their paths, and to each
# element of an ENVIRONMENT value, path lists included.
#
# gtest_discover_tests() suites are carried as the arguments their PRE_TEST
# discovery script passes to gtest_discover_tests_impl() (--discovery), or with
# CMake 4.4 and later the discover_tests() call that replaces it, translated the
# same way; ctest-replay.py lists the cases on the target, as that discovery does,
# and runs each in its own process.
#
# Fixtures are kept for ctest-replay.py to enforce. A test that requires a fixture
# whose setup test is unreplayable is unreplayable too, as ctest would not run it
# without that setup, unless --provided-fixture says the image itself provides
# what the setup would have produced.

import argparse
import json
import os
import re
import sys

SCRATCH = "@SCRATCH@"
# A value is translated element by element, so one need not start with a path:
# cmake -E env --modify takes NAME=OPERATION:/path.
ASSIGNMENT = re.compile(r"^(-D)?([A-Za-z_][A-Za-z0-9_]*(:[A-Z]+)?)=(.*/.*)$")


def dart_timeout(path):
    """ctest's default per-test timeout, the TimeOut key of DartConfiguration.tcl."""
    with open(path, encoding="utf-8") as handle:
        for line in handle:
            if line.startswith("TimeOut:"):
                return int(line.split(":", 1)[1].strip())
    sys.exit("%s has no TimeOut: ctest's default timeout is unknown" % path)


def cmake_true(value):
    return value.upper() in ("1", "ON", "YES", "TRUE", "Y")


def discovery_arguments(path):
    """The arguments a discovery script passes to gtest_discover_tests_impl().

    GoogleTest.cmake writes one KEYWORD per line, each value as a [==[...]==]
    bracket argument, so the values are taken verbatim. From CMake 4.4 a PRE_TEST
    suite is described by a discover_tests() call instead, which
    discover_tests_arguments() maps onto the same keywords."""
    with open(path, encoding="utf-8") as handle:
        text = handle.read()
    if "gtest_discover_tests_impl(" not in text:
        return discover_tests_arguments(path, text)
    call = text[text.index("gtest_discover_tests_impl("):]
    call = call[:call.index("\n)")]
    arguments = {}
    for line in call.splitlines()[1:]:
        match = re.match(r"^\s+([A-Z_]+)\s*(.*)$", line)
        if not match:
            sys.exit("%s: cannot read %r" % (path, line))
        arguments[match.group(1)] = re.findall(r"\[==\[(.*?)\]==\]", match.group(2))
    if "TEST_EXECUTABLE" not in arguments:
        sys.exit("%s: no TEST_EXECUTABLE" % path)
    return arguments


# A discover_tests() argument: a [[...]] bracket argument, a quoted one, or a word.
DISCOVER_TESTS_TOKEN = re.compile(r'\[(=*)\[(.*?)\]\1\]|"((?:[^"\\]|\\.)*)"|([^\s()"]+)', re.S)
DISCOVER_TESTS_SECTIONS = ("COMMAND", "DISCOVERY_ARGS", "DISCOVERY_MATCH", "DISCOVERY_PROPERTIES",
                           "TEST_NAME", "TEST_ARGS", "TEST_PROPERTIES", "TEST_LIST")
# GoogleTest.cmake's own TEST_PROPERTIES, which come before the suite's.
DISCOVER_TESTS_PROPERTIES = ["DISABLED", "DEF_SOURCE_LINE", "SKIP_REGULAR_EXPRESSION",
                             "WORKING_DIRECTORY"]


def discover_tests_arguments(path, text):
    """The gtest_discover_tests_impl() keywords a discover_tests() call amounts to.

    The call carries the discovery script's settings as -D NAME=value arguments of
    COMMAND, DISCOVERY_ARGS and TEST_ARGS, the discovery timeout and working
    directory as DISCOVERY_PROPERTIES, the prefix and suffix around the \\1 of
    TEST_NAME, and the suite's own properties after GoogleTest.cmake's in
    TEST_PROPERTIES. List values are CMake lists."""
    start = re.search(r"^\s*discover_tests\(", text, re.M)
    if not start:
        sys.exit("%s: neither gtest_discover_tests_impl() nor discover_tests()" % path)
    call = text[start.end():]
    call = call[:re.search(r"^\s*\)\s*$", call, re.M).start()]
    sections = {}
    current = None
    for token in DISCOVER_TESTS_TOKEN.finditer(call):
        bracket, quoted, word = token.group(2), token.group(3), token.group(4)
        if word in DISCOVER_TESTS_SECTIONS:
            current = sections.setdefault(word, [])
        elif current is None:
            sys.exit("%s: cannot read %r" % (path, token.group(0)))
        else:
            current.append(next(v for v in (bracket, quoted, word) if v is not None))
    defines = {}
    for section in ("COMMAND", "DISCOVERY_ARGS", "TEST_ARGS"):
        values = sections.get(section, [])
        for flag, value in zip(values, values[1:]):
            if flag == "-D":
                name, _, value = value.partition("=")
                # TEST_ARGS repeats TEST_FILTER as the \2 of DISCOVERY_MATCH.
                defines.setdefault(name, value)
    if "TEST_EXECUTABLE" not in defines:
        sys.exit("%s: no TEST_EXECUTABLE" % path)
    discovery = sections.get("DISCOVERY_PROPERTIES", [])
    discovery = dict(zip(discovery[::2], discovery[1::2]))
    prefix, _, suffix = (sections.get("TEST_NAME") or ["\\1"])[0].partition("\\1")
    properties = sections.get("TEST_PROPERTIES", [])
    if properties[:2 * len(DISCOVER_TESTS_PROPERTIES):2] != DISCOVER_TESTS_PROPERTIES:
        sys.exit("%s: unexpected TEST_PROPERTIES %r" % (path, properties))
    properties = properties[2 * len(DISCOVER_TESTS_PROPERTIES):]

    def cmake_list(value):
        return value.split(";") if value else []

    return {
        "TEST_EXECUTABLE": [defines["TEST_EXECUTABLE"]],
        "TEST_EXECUTOR": cmake_list(defines.get("TEST_EXECUTOR", "")),
        "TEST_WORKING_DIR": [discovery.get("WORKING_DIRECTORY", "")],
        "TEST_FILTER": [defines.get("TEST_FILTER", "")],
        "TEST_EXTRA_ARGS": cmake_list(defines.get("TEST_EXTRA_ARGS", "")),
        "TEST_DISCOVERY_EXTRA_ARGS": cmake_list(defines.get("TEST_DISCOVERY_EXTRA_ARGS", "")),
        "TEST_DISCOVERY_TIMEOUT": [discovery.get("TIMEOUT", "")],
        "TEST_PREFIX": [prefix],
        "TEST_SUFFIX": [suffix],
        "NO_PRETTY_TYPES": [defines.get("NO_PRETTY_TYPES", "")],
        "NO_PRETTY_VALUES": [defines.get("NO_PRETTY_VALUES", "")],
        "TEST_PROPERTIES": properties,
    }


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--tests", action="append", default=[],
                        help="ctest --show-only=json-v1 output; repeatable")
    parser.add_argument("--discovery", action="append", default=[], metavar="FILE",
                        help="a gtest_discover_tests() <target>_<hash>_discovery.cmake, or with "
                             "CMake 4.4 and later its <target>_<hash>_include.cmake; repeatable")
    parser.add_argument("--discovery-root", metavar="DIR",
                        help="where a discovered suite run from the top of the build tree "
                             "runs instead: the installed suites' directory, which holds the "
                             "build-root-relative files they read")
    parser.add_argument("--build-dir", required=True, help="the build tree ctest was run on")
    parser.add_argument("--source-dir", required=True, help="the matching source tree")
    parser.add_argument("--installed-dir",
                        help="where both install to, when the image installs them in one place")
    parser.add_argument("--dart-config", required=True, help="DartConfiguration.tcl of --build-dir")
    parser.add_argument("--map", action="append", default=[], metavar="FROM=TO",
                        help="rewrite a host path to a path the image installs; repeatable")
    parser.add_argument("--tool", action="append", default=[], metavar="FROM=TO",
                        help="rewrite a host tool to the target's own, which another "
                             "package provides and so is not looked for in --image; repeatable")
    parser.add_argument("--run-in", action="append", default=[], metavar="DIR",
                        help="a build-tree working directory to run from its --map target "
                             "rather than from scratch; repeatable")
    parser.add_argument("--run-in-tree", action="append", default=[], metavar="DIR",
                        help="as --run-in, for every working directory at or under DIR; "
                             "repeatable")
    parser.add_argument("--alias", action="append", default=[], metavar="COPY=DIR",
                        help="a copy of the build tree's CTestTestfile.cmake files that ctest "
                             "was run on: a working directory under COPY is the same one under "
                             "DIR; repeatable")
    parser.add_argument("--host-tools-dir",
                        help="HOSTTOOLS_DIR; a command run from here is named for the target's PATH")
    parser.add_argument("--include", help="replay only tests whose name matches this regex")
    parser.add_argument("--exclude", help="leave out tests whose name matches this regex")
    parser.add_argument("--exclude-manifest", action="append", default=[], metavar="MANIFEST",
                        help="leave out tests another manifest already lists; repeatable")
    parser.add_argument("--unreplayable", action="append", default=[], metavar="NAME=REASON",
                        help="a test whose command line looks replayable but which needs what "
                             "the target lacks, with the reason; repeatable")
    parser.add_argument("--provided-fixture", action="append", default=[], metavar="FIXTURE",
                        help="a fixture whose setup the image itself stands in for; repeatable")
    parser.add_argument("--work-dir", required=True, help="no replayed argument may point under here")
    parser.add_argument("--image", required=True, help="D, to tell what is packaged")
    parser.add_argument("--output", required=True)
    args = parser.parse_args()
    if not args.tests and not args.discovery:
        parser.error("nothing to translate: give --tests or --discovery")

    listed = set()
    for other in args.exclude_manifest:
        with open(other, encoding="utf-8") as handle:
            other = json.load(handle)
        listed.update(t["name"] for t in other["tests"] + other["not_replayable"])

    tests = []
    for source_file in args.tests:
        with open(source_file, encoding="utf-8") as handle:
            found = json.load(handle)["tests"]
        if args.include:
            found = [t for t in found if re.search(args.include, t["name"])]
        if args.exclude:
            found = [t for t in found if not re.search(args.exclude, t["name"])]
        found = [t for t in found if t["name"] not in listed]
        # An empty manifest is indistinguishable from a passing run, so refuse it.
        if not found:
            sys.exit("%s defines no selected tests: the manifest would check nothing"
                     % source_file)
        tests += found

    build = os.path.join(args.build_dir, "")
    source = os.path.join(args.source_dir, "")
    installed = os.path.join(args.installed_dir, "") if args.installed_dir else None
    maps = []
    for option, pairs, checked in (("--map", args.map, True), ("--tool", args.tool, False)):
        for pair in pairs:
            old, sep, new = pair.partition("=")
            if not sep:
                sys.exit("%s %s is not FROM=TO" % (option, pair))
            maps.append((old, new, checked))
    # Longest first, so a mapped directory never shadows a file inside it.
    maps.sort(key=lambda entry: len(entry[0]), reverse=True)

    aliases = []
    for pair in args.alias:
        old, sep, new = pair.partition("=")
        if not sep:
            sys.exit("--alias %s is not COPY=DIR" % pair)
        aliases.append((os.path.normpath(old), os.path.normpath(new)))

    def unalias(workdir):
        # ctest gives a test without WORKING_DIRECTORY the directory of the
        # CTestTestfile.cmake it read, which for a copy is not the build tree.
        workdir = os.path.normpath(workdir)
        for old, new in aliases:
            if workdir == old or workdir.startswith(os.path.join(old, "")):
                return new + workdir[len(old):]
        return workdir

    run_in = {os.path.normpath(d) for d in args.run_in}
    run_in_trees = [os.path.join(os.path.normpath(d), "") for d in args.run_in_tree]
    host_tools = os.path.join(args.host_tools_dir, "") if args.host_tools_dir else None

    def runs_in_place(workdir):
        workdir = os.path.normpath(workdir)
        return workdir in run_in or any(os.path.join(workdir, "").startswith(tree)
                                        for tree in run_in_trees)

    def packaged(path):
        return os.path.lexists(args.image + path)

    def candidates(path):
        return [(new + path[len(old):], checked) for old, new, checked in maps
                if path == old or path.startswith(os.path.join(old, ""))]

    class Unreplayable(Exception):
        pass

    def rewrite(path, outputs):
        """The target path for one host path, or Unreplayable."""
        if host_tools and path.startswith(host_tools):
            raise Unreplayable("needs %s, a host tool" % path)
        # The tree itself is never a test input but what an install check
        # inspects, and a --map of the whole tree must not stand in for it.
        if os.path.join(path, "") in (build, source):
            raise Unreplayable("needs the whole of %s" % path)
        tried = []
        for target, checked in candidates(path):
            if not checked or packaged(target):
                return target
            tried.append(target)
        for root in (build, source):
            if not path.startswith(root):
                continue
            relative = path[len(root):]
            if installed and packaged(installed + relative):
                return installed + relative
            if root == build and not os.path.lexists(path):
                # Create only parents that exist in the build tree; a test may need one missing.
                if os.path.isdir(os.path.dirname(path)):
                    outputs.add(os.path.dirname(relative))
                return os.path.join(SCRATCH, relative)
            raise Unreplayable("needs %s, which the image does not install"
                               % (tried[0] if tried else path))
        if tried:
            raise Unreplayable("needs %s, which the image does not install" % tried[0])
        if path.startswith(os.path.join(args.work_dir, "")):
            raise Unreplayable("runs %s, a host tool or unpackaged build file" % path)
        return path

    def translate_paths(value, outputs):
        # A value may be a ':'-joined path list, as PYTHONPATH is, whether it
        # arrives as an ENVIRONMENT entry or as a cmake -E env argument, so each
        # absolute element is translated alone.
        return ":".join(rewrite(e, outputs) if e.startswith("/") else e
                        for e in value.split(":"))

    def translate(argument, outputs):
        if argument.startswith("/"):
            return translate_paths(argument, outputs)
        match = ASSIGNMENT.match(argument)
        if match:
            return argument[:match.start(4)] + translate_paths(match.group(4), outputs)
        return argument

    def translate_environment(entry, outputs):
        name, sep, value = entry.partition("=")
        if not sep:
            raise Unreplayable("has an ENVIRONMENT entry without '=': %s" % entry)
        return name + "=" + translate_paths(value, outputs)

    declared = {}
    for pair in args.unreplayable:
        name, sep, reason = pair.partition("=")
        if not sep or not reason:
            sys.exit("--unreplayable %s is not NAME=REASON" % pair)
        declared[name] = reason
    unknown = sorted(set(declared) - {t["name"] for t in tests})
    if unknown:
        # A stale declaration would hide nothing and mislead whoever reads it.
        sys.exit("--unreplayable names no selected test: %s" % ", ".join(unknown))

    entries, dropped = [], []
    for test in tests:
        properties = {p["name"]: p["value"] for p in test.get("properties", [])}
        name = test["name"]
        if name in declared:
            dropped.append((name, declared[name]))
            continue
        if properties.get("DISABLED"):
            dropped.append((name, "disabled upstream"))
            continue
        if "command" not in test:
            dropped.append((name, "ctest reports no command for it"))
            continue
        unsupported = sorted(set(properties) & {
            "ENVIRONMENT_MODIFICATION",
            "TIMEOUT_AFTER_MATCH", "TIMEOUT_SIGNAL_NAME", "REQUIRED_FILES",
            "GENERATED_RESOURCE_SPEC_FILE", "RESOURCE_GROUPS"})
        if unsupported:
            # ctest-replay.py implements none of these, and silently ignoring one
            # changes the verdict, so a test that sets one is not replayed.
            dropped.append((name, "sets %s, which the replayer does not implement"
                            % ", ".join(unsupported)))
            continue

        outputs = set()
        try:
            command = list(test["command"])
            # ctest resolves an interpreter such as a TEST_LAUNCHER's "sh" or a
            # find_program()'d bash on the build host, where bitbake's PATH puts
            # it in HOSTTOOLS_DIR; the target finds its own by name.
            if host_tools and command and command[0].startswith(host_tools):
                command[0] = os.path.basename(command[0])
            program = command[0] if command else ""
            command = [translate(argument, outputs) for argument in command]
            # A missing build-tree path reads as an output, but the program
            # itself is never one: the build skipped it.
            if command and command[0].startswith(SCRATCH):
                raise Unreplayable("runs %s, which the build did not produce" % program)
            environment = [translate_environment(e, outputs)
                           for e in properties.get("ENVIRONMENT", [])]
            # A build-tree working directory is where the test may write
            # relative paths, so it maps into the scratch tree, as do the build
            # directories every output was written into, which existed when
            # ctest ran. A --run-in directory is the exception: "python -m"
            # puts the working directory first on sys.path, so tests run from
            # their own package directory import from it, and run from its
            # --map target instead.
            workdir = unalias(properties.get("WORKING_DIRECTORY") or args.build_dir)
            if runs_in_place(workdir):
                workdir = rewrite(workdir, outputs)
            elif os.path.join(workdir, "").startswith(build):
                relative = os.path.normpath(os.path.join(workdir, "")[len(build):] or ".")
                outputs.add("" if relative == "." else relative)
                workdir = SCRATCH if relative == "." else os.path.join(SCRATCH, relative)
            else:
                raise Unreplayable("runs from %s, outside the build tree" % workdir)
        except Unreplayable as reason:
            dropped.append((name, str(reason)))
            continue
        leaked = [a for a in command + environment + [workdir]
                  if args.work_dir in a or (host_tools and host_tools in a)]
        if leaked:
            dropped.append((name, "still names a build-host path: %s" % leaked[0]))
            continue
        timeout = properties.get("TIMEOUT")
        entry = {
            "name": name,
            "command": command,
            "working_directory": workdir,
            "scratch_dirs": sorted(d for d in outputs),
            "timeout": float(timeout) if timeout is not None else None,
            "skip_return_code": properties.get("SKIP_RETURN_CODE"),
            "pass_regular_expression": properties.get("PASS_REGULAR_EXPRESSION", []),
            "fail_regular_expression": properties.get("FAIL_REGULAR_EXPRESSION", []),
            "skip_regular_expression": properties.get("SKIP_REGULAR_EXPRESSION", []),
            "will_fail": bool(properties.get("WILL_FAIL", False)),
            "labels": properties.get("LABELS", []),
            "depends": properties.get("DEPENDS", []),
            "fixtures_setup": properties.get("FIXTURES_SETUP", []),
            "fixtures_required": properties.get("FIXTURES_REQUIRED", []),
            "fixtures_cleanup": properties.get("FIXTURES_CLEANUP", []),
        }
        if environment:
            entry["environment"] = environment
        entries.append(entry)

    # ctest runs a fixture's setup tests before any test requiring it and does not
    # run the test when one fails. A setup the target cannot run therefore leaves
    # the test unrunnable too, unless the image provides what it would produce.
    # Dropping a test can strip another fixture of its setup, so repeat until stable.
    provided = set(args.provided_fixture)
    setups = {}
    for test in tests:
        properties = {p["name"]: p["value"] for p in test.get("properties", [])}
        for fixture in properties.get("FIXTURES_SETUP", []):
            setups.setdefault(fixture, []).append(test["name"])
    while True:
        why = dict(dropped)
        kept = []
        for entry in entries:
            missing = [(fixture, setup) for fixture in entry["fixtures_required"]
                       if fixture not in provided
                       for setup in setups.get(fixture, []) if setup in why]
            if missing:
                fixture, setup = missing[0]
                dropped.append((entry["name"], "requires fixture %s, whose setup %s is not "
                                "replayable (%s)" % (fixture, setup, why[setup])))
            else:
                kept.append(entry)
        if len(kept) == len(entries):
            break
        entries = kept
    for entry in entries:
        # The image stands in for these setups, which the target never runs.
        entry["fixtures_required"] = [f for f in entry["fixtures_required"]
                                      if f not in provided]
    stand_ins = {setup: fixture for fixture in provided for setup in setups.get(fixture, [])}
    dropped = [(name, reason if name not in stand_ins else
                "%s; the image's own install provides fixture %s in its place"
                % (reason, stand_ins[name])) for name, reason in dropped]

    discoveries = []
    for discovery in args.discovery:
        arguments = discovery_arguments(discovery)
        single = {key: (values[0] if values else "") for key, values in arguments.items()}
        program = single.get("TEST_TARGET") or os.path.basename(single["TEST_EXECUTABLE"])
        outputs = set()
        try:
            executable = translate(single["TEST_EXECUTABLE"], outputs)
            if executable.startswith(SCRATCH):
                raise Unreplayable("runs %s, which the build did not produce"
                                   % single["TEST_EXECUTABLE"])
            executor = [translate(a, outputs) for a in arguments.get("TEST_EXECUTOR", []) if a]
            workdir = os.path.normpath(single["TEST_WORKING_DIR"] or args.build_dir)
            if workdir == os.path.normpath(args.build_dir) and args.discovery_root:
                workdir = args.discovery_root
            elif os.path.join(workdir, "").startswith(build):
                relative = os.path.normpath(workdir[len(build):])
                outputs.add(relative)
                workdir = os.path.join(SCRATCH, relative)
            else:
                raise Unreplayable("runs from %s, outside the build tree" % workdir)
            # TEST_PROPERTIES is the NAME value ... list set_tests_properties() takes.
            values = arguments.get("TEST_PROPERTIES", [])
            values = [] if values == [""] else values
            if len(values) % 2:
                raise Unreplayable("has TEST_PROPERTIES that are not NAME value pairs")
            properties = {}
            for name, value in zip(values[::2], values[1::2]):
                properties[name] = value.split(";") if name in (
                    "ENVIRONMENT", "LABELS", "SKIP_REGULAR_EXPRESSION",
                    "PASS_REGULAR_EXPRESSION", "FAIL_REGULAR_EXPRESSION") else value
            unsupported = sorted(set(properties) & {
                "ENVIRONMENT_MODIFICATION", "FIXTURES_REQUIRED", "FIXTURES_SETUP",
                "FIXTURES_CLEANUP", "DEPENDS", "TIMEOUT_AFTER_MATCH", "TIMEOUT_SIGNAL_NAME",
                "REQUIRED_FILES", "RESOURCE_GROUPS"})
            if unsupported:
                raise Unreplayable("sets %s, which the replayer does not implement"
                                   % ", ".join(unsupported))
            if "ENVIRONMENT" in properties:
                properties["ENVIRONMENT"] = [translate_environment(e, outputs)
                                             for e in properties["ENVIRONMENT"]]
            extra = [translate(a, outputs) for a in arguments.get("TEST_EXTRA_ARGS", []) if a]
            discovery_extra = [translate(a, outputs)
                               for a in arguments.get("TEST_DISCOVERY_EXTRA_ARGS", []) if a]
        except Unreplayable as reason:
            dropped.append((program, str(reason)))
            continue
        leaked = [a for a in [executable, workdir] + executor + extra + discovery_extra
                  + properties.get("ENVIRONMENT", [])
                  if args.work_dir in a or (host_tools and host_tools in a)]
        if leaked:
            dropped.append((program, "still names a build-host path: %s" % leaked[0]))
            continue
        discoveries.append({
            "program": program,
            "executor": executor,
            "executable": executable,
            "working_directory": workdir,
            "scratch_dirs": sorted(outputs),
            "prefix": single.get("TEST_PREFIX", ""),
            "suffix": single.get("TEST_SUFFIX", ""),
            "filter": single.get("TEST_FILTER", ""),
            "extra_args": extra,
            "discovery_extra_args": discovery_extra,
            "discovery_timeout": float(single.get("TEST_DISCOVERY_TIMEOUT") or 5),
            "no_pretty_types": cmake_true(single.get("NO_PRETTY_TYPES", "")),
            "no_pretty_values": cmake_true(single.get("NO_PRETTY_VALUES", "")),
            "properties": properties,
        })

    def spell(reason):
        reason = reason.replace(args.work_dir, "${WORKDIR}")
        if args.host_tools_dir:
            reason = reason.replace(args.host_tools_dir, "${HOSTTOOLS_DIR}")
        return reason

    manifest = {
        "generated_from": "ctest --show-only=json-v1 and gtest_discover_tests() discovery",
        "default_timeout": dart_timeout(args.dart_config),
        "tests": sorted(entries, key=lambda entry: entry["name"]),
        "discoveries": sorted(discoveries, key=lambda entry: entry["program"]),
        # Reasons name host paths; WORKDIR and HOSTTOOLS_DIR would leak the
        # build host into the package, so they are spelled symbolically.
        "not_replayable": [{"name": n, "reason": spell(r)} for n, r in sorted(dropped)],
    }
    with open(args.output, "w", encoding="utf-8") as out:
        json.dump(manifest, out, indent=1)
        out.write("\n")
    print("%s: %d replayable, %d discovered suites, %d not"
          % (os.path.basename(args.output), len(entries), len(discoveries), len(dropped)))


if __name__ == "__main__":
    main()
