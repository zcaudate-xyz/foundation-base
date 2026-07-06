(ns documentation.hara-runtime
  (:use code.test))

[[:hero {:title "hara.runtime"
         :subtitle "Runtime adapters for generated code."
         :lead "`hara.runtime` executes generated code in local processes, browsers, databases, editors, OpenResty, Redis, Solidity, Python, and other runtime hosts."}]]

[[:chapter {:title "Motivation"}]]
"Emission alone is not enough for tests and systems work. Runtime adapters let hara.lang run target code, verify behavior, and connect generated functions to real services."

[[:chapter {:title "Internal usage"}]]
"The CI workflow pulls runtime images for Hara tests and installs runtime dependencies for language-specific test groups. Walkthrough live examples use runtime contexts to execute emitted JS and Lua."

[[:chapter {:title "API"}]]
