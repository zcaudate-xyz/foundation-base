(ns documentation.hara-index)

[[:hero {:title "hara"
         :subtitle "Language tooling, runtime integration, and polyglot system libraries."
         :lead "`hara` groups the language-oriented parts of foundation into focused subcategories. `hara.lang` is the compiler and emission layer; `hara.model` defines target language models; `hara.runtime` connects generated code to live systems; and supporting namespaces keep shared grammar, type, and utility behavior reusable."
         :badges ["Language tooling" "Runtimes" "Models" "Polyglot"]
         :actions [{:label "Read the introduction" :href "introduction.html" :variant :primary}
                   {:label "See the comparison" :href "comparison.html"}]}]]

[[:card-grid {:title "Subcategories"
              :lead "The Hara namespaces are organized by role, similar to the focused `std.*` library pages."
              :items [{:meta "Compiler"
                       :title "hara.lang"
                       :text "Book-based authoring, grammar-driven emission, scripts, modules, and language library support."
                       :href "introduction.html"}
                      {:meta "Language Models"
                       :title "hara.model"
                       :text "Target language specifications for JavaScript, Lua, Python, SQL, Solidity, xtalk, and annex languages."}
                      {:meta "Runtimes"
                       :title "hara.runtime"
                       :text "Runtime clients and execution adapters for browsers, databases, containers, editors, and native tools."}
                      {:meta "Shared Emitters"
                       :title "hara.common"
                       :text "Shared grammar, emit, preprocess, and utility behavior used by language models."}
                      {:meta "Typing"
                       :title "hara.typed"
                       :text "Typed analysis and cross-runtime type support for generated systems."}]}]]

[[:callout {:tone :info
            :title "Why this site exists"
            :content "Most tools explain code generation from the perspective of a compiler. This site explains `hara` from the perspective of **systems work**: shared abstractions, target-specific emission, runtime integration, and a single maintenance workflow across languages."}]]

[[:demo {:title "The authoring model in one screen"
          :content "A `script` establishes the language context, while emission turns a shared form model into target syntax."
          :lang "clojure"
          :code "(require '[hara.lang :as l]\n         '[hara.model.spec-js :as js])\n\n(l/emit-as :js '(+ 1 2 3))\n;; => \"1 + 2 + 3\""}]]
