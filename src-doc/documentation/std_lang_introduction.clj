(ns documentation.std-lang-introduction)

[[:hero {:title "Introduction"
         :subtitle "Understanding the book, the grammar, and the runtime model."
         :lead "`std.lang` is best understood as a **language-oriented templating system** built for real multi-language applications. It stores code in a reusable intermediate form, emits that form through a target grammar, and can then hand the result to a matching runtime."
         :badges ["Book model" "Grammar model" "Shared DSL"]}]]

[[:chapter {:title "The core idea"}]]

"Instead of writing and maintaining every target language by hand, `std.lang` lets you author code in a Lisp DSL and describe each target language as a grammar. That grammar controls **how forms are emitted**, **which macros expand**, **which operators exist**, and **what dependencies or native fragments are required**."

[[:card-grid {:items [{:meta "Book"
                       :title "A language is a structured library"
                       :text "A `Book` stores the target language identity, grammar, parent relationship, modules, and code entries. That means emitted code is traceable back to named modules and functions instead of being anonymous text."}
                      {:meta "Grammar"
                       :title "Syntax is data"
                       :text "Reserved operators, blocks, data literals, top-level forms, and special emit hooks are declared in the grammar, which makes a new target language mostly a matter of defining conventions."}
                      {:meta "Script"
                       :title "Namespaces become language modules"
                       :text "Using `l/script` installs a module into the current language book, imports relevant macros, and connects the namespace to a runtime configuration for further evaluation."}]}]]

[[:chapter {:title "Why not just transpile?"}]]

"Straight transpilation is usually about **converting one source language into one target language**. `std.lang` aims at a broader problem: **shared authoring and maintenance across many targets and runtimes**."

[[:callout {:tone :success
            :title "The leverage point"
            :content "Once code is stored as reusable entries inside a language book, you can do more than print it. You can **track dependencies**, **inherit grammars**, **re-stage templates per target**, **inspect modules**, and **run the result inside different runtime adapters**."}]]

[[:chapter {:title "Where it fits well"}]]

[[:card-grid {:items [{:meta "Polyglot systems"
                       :title "Shared logic across JS, Lua, Python, and more"
                       :text "A single conceptual model can be emitted to different environments when those environments share enough structure."}
                      {:meta "Infrastructure"
                       :title "Tight feedback loops"
                       :text "Because authoring, testing, and maintenance stay in Clojure, teams can iterate on generated code without jumping across unrelated toolchains for every edit."}
                      {:meta "Exploration"
                       :title "New targets are incremental"
                       :text "Adding a target language does not require rebuilding the world. A grammar, a runtime adapter, and a set of useful library modules are often enough to make it productive."}]}]]

[[:demo {:title "The mental model"
          :lang "text"
          :code "shared forms -> preprocess + stage -> grammar-driven emission -> target code -> runtime adapter"}]]
