#!/usr/bin/env python3
# SPDX-FileCopyrightText: Copyright (c) 2026 NVIDIA CORPORATION & AFFILIATES.
# SPDX-License-Identifier: MIT
#
# Run the tests in a manifest written by gen-ctest-manifest.py and judge each one
# exactly as ctest would, printing ptest result lines (PASS:/FAIL:/SKIP:).
#
# The verdict is a port of cmCTestRunTest::EndTest() from CMake 4.4.3, the ctest
# the manifest was generated with:
#   - a test killed by its timeout fails, whatever else is set;
#   - a test killed by a signal fails, whatever else is set;
#   - otherwise it is forced to fail when PASS_REGULAR_EXPRESSION is set and none
#     matches, or when any FAIL_REGULAR_EXPRESSION matches; it succeeds when not
#     forced to fail and either it exited 0 or a PASS_REGULAR_EXPRESSION is set;
#     an exit status equal to SKIP_RETURN_CODE, or a SKIP_REGULAR_EXPRESSION that
#     matches, skips it, even when forced to fail;
#     and it passes when its success differs from WILL_FAIL.
# The expressions are searched in the whole output, stdout and stderr merged in
# arrival order, assembled the way cmProcess does it: split at '\n' or '\0',
# trailing '\r' dropped, each line given back its '\n', the final partial line
# kept as is, and lines carrying <CTestDetails> or <CTestLabel> left out. CMake's
# regex engine lets '.' match a newline, hence re.DOTALL.
#
# Ordering and fixtures follow cmCTestTestHandler::UpdateForFixtures() and
# cmCTestMultiProcessHandler of the same release, run one test at a time:
#   - a test starts only after every test it DEPENDS on has finished, whatever
#     their result; a dependency outside the run is ignored;
#   - a test requiring a fixture also depends on the fixture's setup tests, and is
#     not run, which fails it, when one of them failed; a skipped setup counts as
#     passed, as ctest's EndTest() treats it;
#   - a fixture's cleanup tests run after its setup tests and every test requiring
#     it, whatever their results;
#   - selecting tests with -R or -L adds the setup and cleanup tests of every
#     fixture a selected test requires, transitively.
# Tests linked by a fixture share one scratch tree, as they share the build tree
# under ctest; any other test gets its own.
#
# A gtest_discover_tests() suite is discovered as its PRE_TEST script does in
# GoogleTestAddTests.cmake of the same release: the program is listed with
# --gtest_list_tests (and --gtest_output=json, read in preference to the text),
# the suite's TEST_FILTER and discovery arguments, in its working directory and
# under its discovery timeout. Each case becomes a test of its own, named
# TEST_PREFIX Suite.Case TEST_SUFFIX with pretty type and value parameters, run as
# PROGRAM --gtest_filter=Suite.Case --gtest_also_run_disabled_tests EXTRA_ARGS with
# SKIP_REGULAR_EXPRESSION "\[  SKIPPED \]" and then the suite's TEST_PROPERTIES;
# a DISABLED_ case is not run. Results are reported as program:Suite.Case, the
# name gtest itself prints.

import argparse
import json
import os
import re
import shutil
import signal
import subprocess
import sys
import tempfile

CTEST_TAGS = (re.compile(r"<CTestDetails>(.*)</CTestDetails>"),
              re.compile(r"<CTestLabel>(.*)</CTestLabel>"))


def ctest_output(raw):
    """The text ctest searches, built from a test's raw merged output."""
    text = raw.decode("utf-8", errors="replace")
    pieces = re.split(r"[\n\0]", text)
    last = pieces.pop()
    lines = [piece.rstrip("\r") for piece in pieces]

    def keep(line):
        return not ("<CTest" in line and any(tag.search(line) for tag in CTEST_TAGS))

    out = "".join(line + "\n" for line in lines if keep(line))
    if last and keep(last):
        out += last + "\n"
    return out


def run(test, scratch, default_timeout, kill_after=0):
    """Run one test; return (status, detail, output).

    ctest kills a test outright at its timeout. With kill_after, it is sent
    SIGTERM first and SIGKILL only that many seconds later, as timeout -k does."""
    def place(value):
        return value.replace("@SCRATCH@", scratch)

    for directory in test["scratch_dirs"]:
        os.makedirs(os.path.join(scratch, directory), exist_ok=True)
    command = [place(argument) for argument in test["command"]]
    workdir = place(test["working_directory"])
    timeout = test["timeout"] if test["timeout"] is not None else default_timeout
    # ENVIRONMENT sets each NAME=value on top of the environment ctest itself
    # was started with, replacing any inherited value of the same name.
    environment = dict(os.environ)
    for entry in test.get("environment", []):
        name, _, value = entry.partition("=")
        environment[name] = place(value)

    try:
        process = subprocess.Popen(command, cwd=workdir, env=environment,
                                   stdin=subprocess.DEVNULL,
                                   stdout=subprocess.PIPE, stderr=subprocess.STDOUT,
                                   start_new_session=True)
    except OSError as error:
        # ctest reports a test it could not start as Not Run, which fails.
        return "FAIL", "could not start: %s" % error, ""
    # ctest's test is over when the process has exited *and* its output pipe has
    # closed, so a background child still holding the pipe keeps it running into
    # its timeout. communicate() waits for exactly both.
    timed_out = False
    try:
        raw, _ = process.communicate(timeout=timeout if timeout > 0 else None)
    except subprocess.TimeoutExpired:
        timed_out = True
        killed_late = False
        try:
            os.killpg(process.pid, signal.SIGTERM if kill_after > 0 else signal.SIGKILL)
        except ProcessLookupError:
            pass
        try:
            raw, _ = process.communicate(timeout=kill_after if kill_after > 0 else None)
        except subprocess.TimeoutExpired:
            killed_late = True
            try:
                os.killpg(process.pid, signal.SIGKILL)
            except ProcessLookupError:
                pass
            raw, _ = process.communicate()
    returncode = process.returncode
    # Anything the test left running in its session would hold the next test's
    # resources; ctest does not reap these, but no verdict depends on it.
    try:
        os.killpg(process.pid, signal.SIGKILL)
    except ProcessLookupError:
        pass
    output = ctest_output(raw)

    if timed_out:
        if killed_late:
            return ("FAIL", "timed out after %gs, killed after another %gs"
                    % (timeout, kill_after), output)
        return "FAIL", "timed out after %gs" % timeout, output
    if returncode < 0:
        return "FAIL", "killed by %s" % signal.Signals(-returncode).name, output

    pass_res = test["pass_regular_expression"]
    fail_res = test["fail_regular_expression"]
    force_fail, reason = False, ""
    if pass_res:
        if not any(re.search(p, output, re.DOTALL) for p in pass_res):
            force_fail, reason = True, "required regular expression not found"
    for pattern in fail_res:
        if re.search(pattern, output, re.DOTALL):
            force_fail, reason = True, "error regular expression found"
            break

    success = not force_fail and (returncode == 0 or bool(pass_res))
    skip = test["skip_return_code"]
    if skip is not None and int(skip) >= 0 and int(skip) == returncode:
        return "SKIP", "SKIP_RETURN_CODE=%d" % int(skip), output
    for pattern in test["skip_regular_expression"]:
        if re.search(pattern, output, re.DOTALL):
            return "SKIP", "skip regular expression found", output
    if success != test["will_fail"]:
        return "PASS", "", output
    if not reason:
        reason = "exited %d" % returncode
    if test["will_fail"]:
        reason += ", WILL_FAIL is set"
    return "FAIL", reason, output


CYCLE = object()


def list_cases(discovery, workdir, listing):
    """(suite, name, type_param, value_param) for each case, or raise RuntimeError."""
    command = list(discovery["executor"]) + [discovery["executable"], "--gtest_list_tests",
                                             "--gtest_output=json:" + listing]
    if discovery["filter"]:
        command.append("--gtest_filter=" + discovery["filter"])
    command += discovery["discovery_extra_args"]
    try:
        result = subprocess.run(command, cwd=workdir, stdin=subprocess.DEVNULL,
                                stdout=subprocess.PIPE, stderr=subprocess.STDOUT,
                                timeout=discovery["discovery_timeout"])
    except subprocess.TimeoutExpired:
        raise RuntimeError("listing timed out after %gs" % discovery["discovery_timeout"])
    except OSError as error:
        raise RuntimeError("could not start: %s" % error)
    output = result.stdout.decode("utf-8", errors="replace")
    if result.returncode != 0:
        raise RuntimeError("listing exited %d:\n%s" % (result.returncode, output))
    cases = []
    if os.path.exists(listing):
        with open(listing, encoding="utf-8") as handle:
            for suite in json.load(handle).get("testsuites", []):
                for case in suite.get("testsuite", []):
                    if case:
                        cases.append((suite["name"], case["name"], case.get("type_param", ""),
                                      case.get("value_param", "")))
        return cases
    suite, type_param = None, ""
    for line in output.splitlines():
        if "gtest_main.cc" in line or not line:
            continue
        if not line.startswith("  "):
            suite = re.sub(r"\.( *#.*)?$", "", line)
            match = re.search(r"# *TypeParam = (.*)$", line)
            type_param = match.group(1) if match else ""
        else:
            name = re.sub(r" ( *#.*)?$", "", line.strip())
            match = re.search(r"# *GetParam\(\) = (.*)$", line)
            cases.append((suite, name, type_param, match.group(1) if match else ""))
    return cases


def discovered_tests(discovery, workdir, cases):
    """The test entries ctest would hold for one discovered suite."""
    properties = discovery["properties"]
    tests = []
    for suite, name, type_param, value_param in cases:
        gtest_name = "%s.%s" % (suite, name)
        pretty_suite, pretty_name = suite, name
        disabled = bool(re.match("^DISABLED_", suite) or re.match("^DISABLED_", name))
        pretty_suite = re.sub("^DISABLED_", "", pretty_suite)
        pretty_name = re.sub("^DISABLED_", "", pretty_name)
        if value_param and not discovery["no_pretty_values"]:
            match = re.match(r"^(.*)/[0-9]+$", pretty_name)
            if match:
                pretty_name = "%s/%s" % (match.group(1), value_param)
        if type_param:
            match = re.match(r"^(.+)/(.+)$", pretty_suite)
            type_name = ""
            if match:
                pretty_suite, type_name = match.group(1), match.group(2)
            if not discovery["no_pretty_types"]:
                pretty_name += "<%s>" % type_param
            elif type_name:
                pretty_name += "<%s>" % type_name
        timeout = properties.get("TIMEOUT")
        skip_code = properties.get("SKIP_RETURN_CODE")
        tests.append({
            "name": "%s%s.%s%s" % (discovery["prefix"], pretty_suite, pretty_name,
                                   discovery["suffix"]),
            "report": "%s:%s" % (discovery["program"], gtest_name),
            "command": list(discovery["executor"]) + [
                discovery["executable"], "--gtest_filter=" + gtest_name,
                "--gtest_also_run_disabled_tests"] + discovery["extra_args"],
            "working_directory": workdir,
            "scratch_dirs": [],
            "timeout": float(timeout) if timeout not in (None, "") else None,
            "skip_return_code": int(skip_code) if skip_code not in (None, "") else None,
            "pass_regular_expression": properties.get("PASS_REGULAR_EXPRESSION", []),
            "fail_regular_expression": properties.get("FAIL_REGULAR_EXPRESSION", []),
            "skip_regular_expression": properties.get("SKIP_REGULAR_EXPRESSION",
                                                      [r"\[  SKIPPED \]"]),
            "will_fail": str(properties.get("WILL_FAIL", "")).upper() in
                         ("1", "ON", "YES", "TRUE", "Y"),
            "labels": properties.get("LABELS", []),
            "environment": properties.get("ENVIRONMENT", []),
            "disabled": disabled or str(properties.get("DISABLED", "")).upper() in
                        ("1", "ON", "YES", "TRUE", "Y"),
            "scratch_group": "discovered-" + discovery["program"],
        })
    return tests


def fixtures(test, kind):
    return test.get("fixtures_" + kind, [])


def ordering(tests):
    """Each test's dependencies, and the setups it needs to have passed, by name."""
    present = {t["name"] for t in tests}
    setups, requirers = {}, {}
    for test in tests:
        for fixture in fixtures(test, "setup"):
            setups.setdefault(fixture, []).append(test["name"])
        for fixture in fixtures(test, "required"):
            requirers.setdefault(fixture, []).append(test["name"])
    depends, require_success = {}, {}
    for test in tests:
        name = test["name"]
        needed = [s for f in fixtures(test, "required") for s in setups.get(f, []) if s != name]
        after = [d for d in test.get("depends", []) if d in present and d != name] + needed
        for fixture in fixtures(test, "cleanup"):
            after += [t for t in setups.get(fixture, []) + requirers.get(fixture, [])
                      if t != name]
        depends[name] = set(after)
        require_success[name] = sorted(set(needed))
    return depends, require_success


def run_order(tests, depends):
    """The tests in manifest order, each after everything it depends on."""
    pending = list(tests)
    done = set()
    while pending:
        ready = next((t for t in pending if depends[t["name"]] <= done), None)
        if ready is None:
            # ctest refuses a cycle outright; fail what is caught in it.
            depends[CYCLE] = {t["name"] for t in pending}
            ready = pending[0]
        pending.remove(ready)
        done.add(ready["name"])
        yield ready


def scratch_groups(tests):
    """A scratch directory name per test, shared by tests linked by a fixture."""
    parent = {}

    def find(key):
        while parent.setdefault(key, key) != key:
            key = parent[key]
        return key

    for test in tests:
        linked = [("fixture", f) for kind in ("setup", "required", "cleanup")
                  for f in fixtures(test, kind)]
        for key in linked:
            parent[find(key)] = find(("test", test["name"]))
    group = {}
    for test in tests:
        root = find(("test", test["name"]))
        # A discovered case runs in its suite's directory, like the listing did.
        group[test["name"]] = test.get("scratch_group") or root[1].replace("/", "_")
    return group


def main():
    parser = argparse.ArgumentParser(description=__doc__,
                                     formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("manifest")
    parser.add_argument("-R", "--tests-regex", help="run only tests whose name matches")
    parser.add_argument("-L", "--label-regex", help="run only tests with a matching label")
    parser.add_argument("--prefix", default="", help="prepended to each reported name")
    parser.add_argument("--scratch", help="scratch tree; default a fresh temporary directory")
    parser.add_argument("--keep", action="store_true", help="keep the scratch tree")
    parser.add_argument("--list", action="store_true", help="list the tests and exit")
    parser.add_argument("--kill-after", type=float, default=0, metavar="SECONDS",
                        help="at a timeout, SIGTERM and SIGKILL only this much later")
    args = parser.parse_args()

    with open(args.manifest, encoding="utf-8") as handle:
        manifest = json.load(handle)

    scratch_root = args.scratch or tempfile.mkdtemp(prefix="ctest-replay.")
    # PRE_TEST discovery: list every discovered suite before any test runs.
    every = list(manifest["tests"])
    undiscovered = []
    for discovery in manifest.get("discoveries", []):
        scratch = os.path.join(scratch_root, "discovered-" + discovery["program"])
        for directory in discovery["scratch_dirs"]:
            os.makedirs(os.path.join(scratch, directory), exist_ok=True)
        workdir = discovery["working_directory"].replace("@SCRATCH@", scratch)
        try:
            cases = list_cases(discovery, workdir,
                               os.path.join(scratch_root, discovery["program"] + ".json"))
        except RuntimeError as error:
            undiscovered.append((discovery["program"], str(error)))
            continue
        every += discovered_tests(discovery, discovery["working_directory"], cases)

    names = {t["name"] for t in every
             if (not args.tests_regex or re.search(args.tests_regex, t["name"]))
             and (not args.label_regex
                  or any(re.search(args.label_regex, label) for label in t["labels"]))}
    while True:
        required = {f for t in every if t["name"] in names for f in fixtures(t, "required")}
        more = {t["name"] for t in every
                if required & (set(fixtures(t, "setup")) | set(fixtures(t, "cleanup")))}
        if more <= names:
            break
        names |= more
    tests = [t for t in every if t["name"] in names]
    if args.list:
        for test in tests:
            print(test.get("report") or test["name"])
        for entry in manifest["not_replayable"]:
            print("%s (not replayable: %s)" % (entry["name"], entry["reason"]))
        return 0

    depends, require_success = ordering(tests)
    groups = scratch_groups(tests)
    rc = 0
    results = {}
    try:
        for program, error in undiscovered:
            # ctest cannot read the suite's tests at all, which fails the run.
            rc = 1
            print("FAIL: %s%s (discovery failed)" % (args.prefix, program), flush=True)
            for line in error.splitlines():
                print("  | " + line)
        for test in run_order(tests, depends):
            # Tests run one at a time, which also honours RUN_SERIAL and
            # RESOURCE_LOCK; each gets its own scratch tree, as each ctest run of
            # a build tree starts from outputs no other test wrote, unless a
            # fixture links it to others.
            scratch = os.path.join(scratch_root, groups[test["name"]])
            os.makedirs(scratch, exist_ok=True)
            failed = [d for d in require_success[test["name"]] if results.get(d) == "FAIL"]
            if test["name"] in depends.get(CYCLE, ()):
                status, detail, output = "FAIL", "circular test dependency", ""
            elif failed:
                status, detail, output = ("FAIL", "fixture dependency failed: %s"
                                          % " ".join(failed), "")
            elif test.get("disabled"):
                # ctest reports it Not Run (Disabled), which is no failure.
                status, detail, output = "SKIP", "disabled upstream", ""
            else:
                status, detail, output = run(test, scratch, manifest["default_timeout"],
                                             args.kill_after)
            results[test["name"]] = status
            name = test.get("report") or args.prefix + test["name"]
            print("%s: %s%s" % (status, name, " (%s)" % detail if detail else ""), flush=True)
            if status == "FAIL":
                rc = 1
                for line in output.splitlines():
                    print("  | " + line)
                sys.stdout.flush()
        if not args.tests_regex and not args.label_regex:
            for entry in manifest["not_replayable"]:
                print("SKIP: %s%s (not replayable: %s)"
                      % (args.prefix, entry["name"], entry["reason"]), flush=True)
    finally:
        if not args.keep:
            shutil.rmtree(scratch_root, ignore_errors=True)
    return rc


if __name__ == "__main__":
    sys.exit(main())
