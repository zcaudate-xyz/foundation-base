(ns documentation.lang-index
  (:require [lang.core :as l])
  (:use code.test))

[[:hero {:title "lang"
         :subtitle "Language tooling, runtime integration, and polyglot system libraries."
         :lead "`hara` groups the language-oriented parts of foundation: `tahto.core` for authoring and emission, `tahto.model` for target specs, `tahto.runtime` for execution, `tahto.typed` for typed xtalk, and examples for generated projects."
         :badges ["Language tooling" "Runtimes" "Models" "Polyglot"]
         :actions [{:label "Read the introduction" :href "introduction.html" :variant :primary}
                   {:label "Browse examples" :href "examples.html"}]}]]

[[:section {:title "First steps"}]]

"`lang.core` lets you write code once in Clojure data and emit it to many targets. The smallest useful program installs a runtime, defines a function, and emits it."

(fact "install a JS runtime and emit a function"
  ^{:refer lang.core/emit-as :added "4.0"}
  (l/emit-as :js '[(defn greet [name]
                     (return (+ "Hello, " name)))
                   (greet "lang")])
  => "function greet(name){\n  return \"Hello, \" + name;\n}\n\ngreet(\"lang\")")

"The same form can be emitted to Lua by changing the language keyword. The grammar and template take care of syntax, statement terminators, and string operators."

(fact "emit the same logic to Lua"
  ^{:refer lang.core/emit-as :added "4.0"}
  (l/emit-as :lua '[(defn greet [name]
                      (return (cat "Hello, " name)))
                    (greet "lang")])
  => "local function greet(name)\n  return 'Hello, ' .. name\nend\n\ngreet(\"lang\")")

[[:card-grid {:title "Subcategories"
              :lead "The Lang section covers the compiler, target models, runtimes, typed analysis, shared emitters, seed generation, and examples."
              :items [{:meta "Compiler"
                       :title "lang.core"
                       :text "Book-based authoring, grammar-driven emission, scripts, modules, and language libraries."
                       :href "introduction.html"}
                      {:meta "Language Models"
                       :title "lang.model"
                       :text "Target specifications for JavaScript, Lua, Python, Go, Dart, SQL, Solidity, xtalk, and annex languages."
                       :href "lang-model.html"}
                      {:meta "Runtimes"
                       :title "lang.runtime"
                       :text "Runtime clients and execution adapters for browsers, databases, containers, editors, and native tools."
                       :href "lang-runtime.html"}
                      {:meta "Typing"
                       :title "lang.typed"
                       :text "Typed xtalk analysis and target declaration emission."
                       :href "lang-typed.html"}
                      {:meta "Shared Emitters"
                       :title "lang.base"
                       :text "Shared grammar, emit, preprocess, rewrite, and utility behavior."
                       :href "lang-common.html"}
                      {:meta "Generation"
                       :title "lang.seedgen"
                       :text "Seed generation and xtalk test scaffolding."
                       :href "lang-seedgen.html"}
                      {:meta "Examples"
                       :title "Generated projects"
                       :text "Play projects and walkthroughs from src-build."
                       :href "examples.html"}]}]]

[[:callout {:tone :info
            :title "Relationship to xt"
            :content "`lang` provides the compiler, type, model, and runtime machinery. `xt` is a portable library/application layer built on top of that machinery."}]]
