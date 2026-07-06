(ns documentation.hara.lang-introduction
  (:require [hara.lang :as l])
  (:use code.test))

[[:hero {:title "Introduction"
         :subtitle "Understanding the book, the grammar, and the runtime model."
         :lead "`hara.lang` is best understood as a **language-oriented templating system** built for real multi-language applications. It stores code in a reusable intermediate form, emits that form through a target grammar, and can then hand the result to a matching runtime."
         :badges ["Book model" "Grammar model" "Shared DSL"]}]]

[[:chapter {:title "The core idea"}]]

"Instead of writing and maintaining every target language by hand, `hara.lang` lets you author code in a Lisp DSL and describe each target language as a grammar. That grammar controls **how forms are emitted**, **which macros expand**, **which operators exist**, and **what dependencies or native fragments are required**."

[[:card-grid {:items [{:meta "Book"
                       :title "A language is a structured library"
                       :text "A `Book` stores the target language identity, grammar, parent relationship, modules, and code entries. That means emitted code is traceable back to named modules and functions instead of being anonymous text."}
                      {:meta "Grammar"
                       :title "Syntax is data"
                       :text "Reserved operators, blocks, data literals, top-level forms, and special emit hooks are declared in the grammar, which makes a new target language mostly a matter of defining conventions."}
                      {:meta "Script"
                       :title "Namespaces become language modules"
                       :text "Using `l/script` installs a module into the current language book, imports relevant macros, and connects the namespace to a runtime configuration for further evaluation."}]}]]

[[:section {:title "Walkthrough"}]]

"The fastest way to see the book and grammar model is to emit the same small function to two targets. `l/emit-as` takes a language keyword and a quoted hara.lang form, and returns the emitted string."

(fact "emit a function to JavaScript"
  ^{:refer hara.lang/emit-as :added "4.0"}
  (l/emit-as :js '[(defn add [a b]
                     (return (+ a b)))
                   (add 1 2)])
  => "function add(a,b){\n  return a + b;\n}\n\nadd(1,2)")

(fact "emit the same function to Lua"
  ^{:refer hara.lang/emit-as :added "4.0"}
  (l/emit-as :lua '[(defn add [a b]
                      (return (+ a b)))
                    (add 1 2)])
  => "local function add(a,b)\n  return a + b\nend\n\nadd(1,2)")

"`l/script-` installs a script context into the current namespace. Once installed, `!.js` evaluates expressions in place, and `defn.js` stores functions as book entries that can be called like Clojure functions."

(fact "install a script context and call a generated function"
  ^{:refer hara.lang/script- :added "4.0"}
  (do
    (l/script- :js
      {:require [[xt.lang.spec-base :as xt]]})
    (defn.js add [a b]
      (return (+ a b)))
    (add 1 2))
  => "add(1,2)")

[[:chapter {:title "Why not just transpile?"}]]

"Straight transpilation is usually about **converting one source language into one target language**. `hara.lang` aims at a broader problem: **shared authoring and maintenance across many targets and runtimes**."

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
