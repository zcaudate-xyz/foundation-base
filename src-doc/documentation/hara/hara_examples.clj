(ns documentation.hara-examples
  (:require [hara.lang :as l]
            [hara.model.spec-go]
            [std.make :as make :refer [def.make]])
  (:use code.test))

[[:hero {:title "Hara examples"
         :subtitle "Generated project examples from src-build/play."
         :lead "The play projects demonstrate hara.lang as a project generator: C pthreads, Go modules, typed xtalk declarations, TypeScript packages, OpenResty Lua, and Blessed terminal UIs."}]]

[[:chapter {:title "Generated projects"}]]
"Use `src-build/play/*/main.clj` for authored source and `src-build/play/*/build.clj` for generated project configuration. The build namespaces use `std.make` to create Makefiles, package files, generated source files, and runnable project layouts."

[[:chapter {:title "Representative examples"}]]
"Start with Go and TypeScript user-directory examples for typed data, OpenResty examples for Lua runtime integration, and TUI examples for JS/Blessed UI output. Cross-link the xtalk examples from the xt section where relevant."

[[:chapter {:title "Walkthrough" :link "walkthrough"}]]

[[:section {:title "Generating Go source"}]]

"`l/script- :go` installs the Go book into the current namespace. Once installed, `defn.go` creates a function entry and `l/emit-ptr` emits its generated source."

(fact "define and emit a Go function"
  ^{:refer hara.lang/script- :added "4.0"}
  (do
    (l/script- :go)
    (defn.go ^{:- [:string]}
      FormatUserKey
      [:string orgId
       :string userId]
      (return (+ orgId ":" userId)))
    (l/emit-ptr FormatUserKey))
  => "func FormatUserKey(orgId string, userId string)  string {\n  return orgId + \":\" + userId\n}")

[[:section {:title "Generating JavaScript source"}]]

"The same pattern works for JavaScript. `l/script- :js` sets up the JS context, then `defn.js` defines a function."

(fact "define and emit a JavaScript function"
  ^{:refer hara.lang/script- :added "4.0"}
  (do
    (l/script- :js
      {:require [[xt.lang.spec-base :as xt]]})
    (defn.js lookupUser
      [users id]
      (return (. users [id])))
    (l/emit-ptr lookupUser))
  => "export function lookupUser(users,id){\n  return users[id];\n}")

[[:section {:title "Cross-language emission"}]]

"For one-off snippets, `l/emit-as` converts a vector of Clojure forms directly to a target language string without needing to define entries."

(fact "emit the same helper in Go, JavaScript, and Lua"
  ^{:refer hara.lang/emit-as :added "4.0"}
  [(l/emit-as :go '[(defn Add [a b] (return (+ a b)))])
   (l/emit-as :js '[(defn add [a b] (return (+ a b)))])
   (l/emit-as :lua '[(defn add [a b] (return (+ a b)))])]
  => ["func Add(a, b) {\n  return a + b\n}"
      "function add(a,b){\n  return a + b;\n}"
      "local function add(a,b)\n  return a + b\nend"])

[[:section {:title "Inspecting generated libraries"}]]

"`l/grammar` returns the grammar map for a language, and `l/lib:overview` returns a data structure describing all modules currently loaded for that language."

(fact "inspect the Go grammar and library overview"
  ^{:refer hara.lang/grammar :added "4.0"}
  (let [g (l/grammar :go)]
    (every? #(contains? g %) [:macros :reserved :emit :structure]))
  => true

  ^{:refer hara.lang/lib:overview :added "4.0"}
  (map? (l/lib:overview :go))
  => true)

[[:section {:title "Scaffolding a project"}]]

"The play projects wrap generated modules in a `std.make` project. A `def.make` declaration describes the repository, build sections, and output modules; `make/build-all` materialises the files. The build step writes to `.build/`, so it is shown without running."

^{:eval false}
(fact "a minimal std.make project for a Go module"
  (do
    (def.make PROJECT
      {:github   {:repo "example.com/play.go-000-user-directory"
                  :description "Simple Go project generated from Clojure"}
       :orgfile  "Main.org"
       :triggers '#{play.go-000-user-directory.main}
       :sections {:setup [{:type :gitignore
                           :main ["bin"]}
                          {:type :makefile
                           :main [[:.PHONY {:- ["build" "test"]}]
                                  [:build ["go build ./..."]]
                                  [:test ["go test ./..."]]]}]}
       :default [{:type :module.single
                  :lang :go
                  :main 'play.go-000-user-directory.main
                  :file "user_directory.go"
                  :header "package userdirectory"}]})

    (make/build-all PROJECT)))
