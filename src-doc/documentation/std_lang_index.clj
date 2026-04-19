(ns documentation.std-lang-index)

[[:hero {:title "std.lang"
         :subtitle "A language-oriented compiler for multi-runtime systems."
         :lead "`std.lang` treats code generation as **authoring in data**, **emitting through grammars**, and **running against real runtimes**. The result is not just transpilation, but a reusable library model for building polyglot systems from a shared Lisp DSL."
         :badges ["Grammar-driven" "Polyglot" "Runtime-aware" "Template-first"]
         :actions [{:label "Read the introduction" :href "introduction.html" :variant :primary}
                   {:label "See the comparison" :href "comparison.html"}]}]]

[[:callout {:tone :info
            :title "Why this site exists"
            :content "Most tools explain code generation from the perspective of a compiler. This site explains `std.lang` from the perspective of **systems work**: shared abstractions, target-specific emission, runtime integration, and a single maintenance workflow across languages."}]]

[[:card-grid {:title "What makes `std.lang` different"
              :lead "The project sits somewhere between a transpiler, a templating engine, and a language workbench."
              :items [{:meta "Authoring"
                       :title "Code stays structured"
                       :text "Functions and modules are stored as structured entries in a language book, so they can be inspected, linked, re-staged, and re-emitted instead of only being printed once."}
                      {:meta "Emission"
                       :title "Grammars own syntax"
                       :text "Target languages are defined as grammar data: operators, macros, reserved forms, formatting, and language-specific hooks all live in the emitter model."}
                      {:meta "Execution"
                       :title "Runtimes are first-class"
                       :text "Generated code can be executed against browser, terminal, Redis, OpenResty, database, or other target runtimes through a shared runtime control layer."}
                      {:meta "Maintenance"
                       :title "One workflow across targets"
                       :text "Testing, documentation, and code maintenance stay in the same Clojure-centered toolchain even when the emitted output is JS, Lua, Python, Solidity, or beyond."}]}]]

[[:quote {:text "The interesting part of `std.lang` is not that it can print JavaScript or Lua. It is that the same authoring model can keep a multi-language system coherent."
           :source "std.lang design perspective"}]]

[[:demo {:title "The authoring model in one screen"
          :content "A `script` establishes the language context, while emission turns a shared form model into target syntax."
          :lang "clojure"
          :code "(require '[std.lang :as l]\n         '[std.lang.model.spec-js :as js])\n\n(l/emit-as :js '(+ 1 2 3))\n;; => \"1 + 2 + 3\""}]]
