(ns documentation.hara-examples
  (:use code.test))

[[:hero {:title "Hara examples"
         :subtitle "Generated project examples from src-build/play."
         :lead "The play projects demonstrate hara.lang as a project generator: C pthreads, Go modules, typed xtalk declarations, TypeScript packages, OpenResty Lua, and Blessed terminal UIs."}]]

[[:chapter {:title "Generated projects"}]]
"Use `src-build/play/*/main.clj` for authored source and `src-build/play/*/build.clj` for generated project configuration. The build namespaces use `std.make` to create Makefiles, package files, generated source files, and runnable project layouts."

[[:chapter {:title "Representative examples"}]]
"Start with Go and TypeScript user-directory examples for typed data, OpenResty examples for Lua runtime integration, and TUI examples for JS/Blessed UI output. Cross-link the xtalk examples from the xt section where relevant."
