# Copyright 2022 The Bazel Authors. All rights reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
"""Implementation of py_runtime rule."""

def _py_runtime_impl(ctx):
    _ = ctx  # @unused
    fail("not implemented")

# Bind to the name "py_runtime" to preserve the kind/rule_class it shows up
# as elsewhere.
py_runtime = rule(
    implementation = _py_runtime_impl,
    doc = """
Represents a Python runtime used to execute Python code.

A `py_runtime` target can represent either a *platform runtime* or an *in-build
runtime*. A platform runtime accesses a system-installed interpreter at a known
path, whereas an in-build runtime points to an executable target that acts as
the interpreter. In both cases, an "interpreter" means any executable binary or
wrapper script that is capable of running a Python script passed on the command
line, following the same conventions as the standard CPython interpreter.

A platform runtime is by its nature non-hermetic. It imposes a requirement on
the target platform to have an interpreter located at a specific path. An
in-build runtime may or may not be hermetic, depending on whether it points to
a checked-in interpreter or a wrapper script that accesses the system
interpreter.

# Example

```
py_runtime(
    name = "python-2.7.12",
    files = glob(["python-2.7.12/**"]),
    interpreter = "python-2.7.12/bin/python",
)

py_runtime(
    name = "python-3.6.0",
    interpreter_path = "/opt/pyenv/versions/3.6.0/bin/python",
)
```
""",
    fragments = ["py"],
    attrs = {
        "files": attr.label_list(
            allow_files = True,
            doc = """
For an in-build runtime, this is the set of files comprising this runtime.
These files will be added to the runfiles of Python binaries that use this
runtime. For a platform runtime this attribute must not be set.
""",
        ),
        "interpreter": attr.label(
            allow_single_file = True,
            doc = """
For an in-build runtime, this is the target to invoke as the interpreter. For a
platform runtime this attribute must not be set.
""",
        ),
        "interpreter_path": attr.string(doc = """
For a platform runtime, this is the absolute path of a Python interpreter on
the target platform. For an in-build runtime this attribute must not be set.
"""),
        "coverage_tool": attr.label(
            allow_files = False,
            doc = """
This is a target to use for collecting code coverage information from `py_binary`
and `py_test` targets.

If set, the target must either produce a single file or be an executable target.
The path to the single file, or the executable if the target is executable,
determines the entry point for the python coverage tool.  The target and its
runfiles will be added to the runfiles when coverage is enabled.

The entry point for the tool must be loadable by a Python interpreter (e.g. a
`.py` or `.pyc` file).  It must accept the command line arguments
of coverage.py (https://coverage.readthedocs.io), at least including
the `run` and `lcov` subcommands.
""",
        ),
        "python_version": attr.string(
            values = ["PY2", "PY3", "_INTERNAL_SENTINEL"],
            doc = """
Whether this runtime is for Python major version 2 or 3. Valid values are `"PY2"`
and `"PY3"`.

The default value is controlled by the `--incompatible_py3_is_default` flag.
However, in the future this attribute will be mandatory and have no default
value.
            """,
        ),
        "stub_shebang": attr.string(
            # TODO(b/254866025): Have PyRuntimeInfo and this use a shared
            # constant
            default = "#!/usr/bin/env python3",
            doc = """
"Shebang" expression prepended to the bootstrapping Python stub script
used when executing `py_binary` targets.

See https://github.com/bazelbuild/bazel/issues/8685 for
motivation.

Does not apply to Windows.
""",
        ),
    },
)