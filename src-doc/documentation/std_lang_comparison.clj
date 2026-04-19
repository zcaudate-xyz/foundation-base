(ns documentation.std-lang-comparison)

[[:hero {:title "Comparison"
         :subtitle "How `std.lang` differs from straight transpilation and adjacent tools."
         :lead "`std.lang` overlaps with transpilers, DSL toolkits, and language workbenches, but its center of gravity is different: it is optimized for **shared authoring**, **grammar-driven code generation**, and **runtime-aware polyglot workflows**."
         :badges ["Transpilers" "Language workbenches" "Polyglot systems"]}]]

[[:chapter {:title "Versus straight transpilation"}]]

[[:card-grid {:items [{:meta "Straight transpiler"
                       :title "Source fidelity first"
                       :text "A classic transpiler usually starts from one source language and tries to preserve its semantics while producing one primary target language or a fixed set of backends."}
                      {:meta "`std.lang`"
                       :title "Shared abstractions first"
                       :text "`std.lang` starts from a shared DSL and expects the grammar to decide how each target should realize that abstraction. The point is not exact syntax preservation, but reusable cross-target structure."}
                      {:meta "Tradeoff"
                       :title "More leverage, less directness"
                       :text "You gain reuse, inspection, and runtime integration, but you also accept that the authoring language is a deliberate meta-layer rather than the target language itself."}]}]]

[[:callout {:tone :warning
            :title "When a plain transpiler is better"
            :content "If the only goal is to translate one existing language into another with high fidelity to the source ecosystem, a dedicated transpiler is usually simpler. `std.lang` is strongest when you want a **shared generation system** rather than a one-off translation pass."}]]

[[:chapter {:title "Versus nearby systems"}]]

[[:card-grid {:items [{:meta "Haxe / Nim / ReScript"
                       :title "Multi-target compilers"
                       :text "These tools give you one language that targets several runtimes. They are closer on the multi-target axis, but usually less open-ended as programmable grammar systems."}
                      {:meta "Racket"
                       :title "Language-oriented programming"
                       :text "Racket is stronger as a host for new languages and macro systems. `std.lang` is more directly aimed at emitting mainstream target languages and integrating them into shared runtime flows."}
                      {:meta "JetBrains MPS / Spoofax"
                       :title "Language workbenches"
                       :text "Those systems are deeper on language engineering, projectional editing, and formal tooling. `std.lang` is lighter, code-first, and much more comfortable inside a REPL-driven Clojure workflow."}]}]]

[[:chapter {:title "What is unusual about `std.lang`?"}]]

[[:card-grid {:items [{:meta "1"
                       :title "Lisp data as the authoring format"
                       :text "The source representation is simple enough to transform, inspect, and compose without fighting a conventional parser frontend every time."}
                      {:meta "2"
                       :title "Grammar and runtime live together"
                       :text "The language spec does not end at printing syntax. It can also describe how generated code should be evaluated, tested, and embedded in a target environment."}
                      {:meta "3"
                       :title "Template re-staging per target"
                       :text "Entries can be re-hydrated and re-staged for the current grammar, which makes code generation more adaptable than a fixed AST-to-AST pipeline."}
                      {:meta "4"
                       :title "One maintenance surface"
                       :text "Documentation, testing, and code maintenance stay tied to the same source of truth instead of fragmenting across many language ecosystems."}]}]]

[[:quote {:text "The best way to think about `std.lang` is not as a prettier transpiler. It is a reusable authoring and emission system for code that has to live in several languages at once."
           :source "comparison summary"}]]
