# xt.ui Wind Task List

This demo compiles a portable XTalk controller and `xt.ui` view to Dart, passes
that tree through `xt.ui.wind/prepare`, and renders the resulting descriptor and
action bundle with Flutter's `WDynamic` widget.

Generated Flutter and package files live under `.build/demo-wind-task-list`.
They are disposable and should not be edited directly.

```bash
lein run -m demo.wind-task-list.build build
lein run -m demo.wind-task-list.build test
lein run -m demo.wind-task-list.build run-linux
lein run -m demo.wind-task-list.build run-web
```

The Flutter scaffold is limited to Linux and web. The first build creates the
scaffold, emits the local XTalk Dart workspace, and runs `flutter pub get`. Linux
runs require `clang++` and the standard Flutter Linux desktop prerequisites; the
`test` command always builds web and builds Linux when `clang++` is available.
