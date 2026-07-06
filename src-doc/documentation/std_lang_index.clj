(ns documentation.hara-index)

[[:hero {:title "hara"
         :subtitle "Language tooling, runtime integration, and polyglot system libraries."
         :lead "`hara` groups the language-oriented parts of foundation: `hara.lang` for authoring and emission, `hara.model` for target specs, `hara.runtime` for execution, `hara.typed` for typed xtalk, and examples for generated projects."
         :badges ["Language tooling" "Runtimes" "Models" "Polyglot"]
         :actions [{:label "Read the introduction" :href "introduction.html" :variant :primary}
                   {:label "Browse examples" :href "examples.html"}]}]]

[[:card-grid {:title "Subcategories"
              :lead "The Hara section covers the compiler, target models, runtimes, typed analysis, shared emitters, seed generation, and examples."
              :items [{:meta "Compiler"
                       :title "hara.lang"
                       :text "Book-based authoring, grammar-driven emission, scripts, modules, and language libraries."
                       :href "introduction.html"}
                      {:meta "Language Models"
                       :title "hara.model"
                       :text "Target specifications for JavaScript, Lua, Python, Go, Dart, SQL, Solidity, xtalk, and annex languages."
                       :href "hara-model.html"}
                      {:meta "Runtimes"
                       :title "hara.runtime"
                       :text "Runtime clients and execution adapters for browsers, databases, containers, editors, and native tools."
                       :href "hara-runtime.html"}
                      {:meta "Typing"
                       :title "hara.typed"
                       :text "Typed xtalk analysis and target declaration emission."
                       :href "hara-typed.html"}
                      {:meta "Shared Emitters"
                       :title "hara.common"
                       :text "Shared grammar, emit, preprocess, rewrite, and utility behavior."
                       :href "hara-common.html"}
                      {:meta "Generation"
                       :title "hara.seedgen"
                       :text "Seed generation and xtalk test scaffolding."
                       :href "hara-seedgen.html"}
                      {:meta "Examples"
                       :title "Generated projects"
                       :text "Play projects and walkthroughs from src-build."
                       :href "examples.html"}]}]]

[[:callout {:tone :info
            :title "Relationship to xt"
            :content "`hara` provides the compiler, type, model, and runtime machinery. `xt` is a portable library/application layer built on top of that machinery."}]]
