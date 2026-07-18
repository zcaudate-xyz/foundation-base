# Build and Demo Sources

`src-build` is a classpath root for source-owned build definitions and runnable
examples. Generated output belongs in `.build/` or `packages-gen/`, not beside
these sources.

## Layout

- `demo/` contains demonstrations that run from this repository. A demo owns
  its portable application source, build definition, and usage notes here.
- `play/` contains the existing standalone project recipes. These examples
  generate independent projects and retain their `play.*` namespaces.
- `xtalk/` defines generation of the JavaScript and Dart XTalk package
  workspaces under `packages-gen/`.
- `component/`, `meta/`, and `playground/` contain focused build utilities.
- `kmi_repl/` contains the KMI REPL application build.

Keep reusable runtime code in `src-lang`; `src-build/demo` should compose that
runtime code into applications rather than becoming another runtime source
root.

The Wind task-list demo under `demo/wind_task_list` documents its build, test,
and run commands in its own README.
