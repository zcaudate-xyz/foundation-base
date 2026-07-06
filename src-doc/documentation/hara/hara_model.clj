(ns documentation.hara-model
  (:use code.test))

[[:hero {:title "hara.model"
         :subtitle "Target language specifications and xtalk function libraries."
         :lead "`hara.model` contains target specs for JS, Lua, Python, Go, Dart, SQL, Solidity, xtalk, and annex languages. These models are the bridge between structured hara.lang forms and emitted target code."}]]

[[:chapter {:title "Motivation"}]]
"A language model owns target syntax, helper functions, type declarations, and runtime-specific emission rules. Generated projects in `src-build/play` use these models when producing Go, TypeScript, Lua, C, and JS artifacts."

[[:chapter {:title "API"}]]
