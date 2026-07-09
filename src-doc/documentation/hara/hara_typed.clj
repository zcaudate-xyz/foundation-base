(ns documentation.hara-typed
  (:use code.test)
  (:require [hara.typed.xtalk-common :as types]
            [hara.typed.xtalk-infer :as infer]
            [hara.typed.xtalk-parse :as parse]))

[[:hero {:title "hara.typed"
         :subtitle "Typed xtalk analysis and emission."
         :lead "`hara.typed` analyzes xtalk type declarations, records, functions, calls, compatibility, inference, and lowering for generated target declarations."}]]

[[:chapter {:title "Motivation"}]]
"Typed xtalk examples define records and functions once, then emit language-specific declarations such as Go structs or TypeScript `.d.ts` files."

[[:chapter {:title "Examples"}]]
"See `src-build/play/go_001_xtalk_user_directory` and `src-build/play/ts_001_single_source_user_directory` for single-source typed xtalk projects."

[[:chapter {:title "API"}]]

[[:chapter {:title "Walkthrough" :link "walkthrough"}]]

[[:section {:title "Normalizing type forms"}]]

"`hara.typed` represents xtalk types as plain data maps. The `normalize-type` function turns the concise type syntax used in source files into these maps."

(fact "normalize records, arrays, and function types"
  (types/type->data
   (types/normalize-type '[:xt/record
                           ["id" :xt/str]
                           ["tags" [:xt/array :xt/str]]]
                         {}))
  => '{:kind :record
       :fields [{:name "id" :type {:kind :primitive :name :xt/str} :optional? false}
                {:name "tags" :type {:kind :array :item {:kind :primitive :name :xt/str}} :optional? false}]}

  (types/type->data
   (types/normalize-type '[:fn [:xt/int :xt/int] :xt/bool]
                         {}))
  => '{:kind :fn
       :inputs [{:kind :primitive :name :xt/int}
                {:kind :primitive :name :xt/int}]
       :output {:kind :primitive :name :xt/bool}})

[[:section {:title "Parsing a typed namespace"}]]

"`analyze-namespace` reads a namespace that contains `defspec.xt`, `defn.xt`, and related forms and returns a map of specs, functions, macros, and values."

(fact "analyze an xtalk namespace"
  (let [analysis (parse/analyze-namespace 'hara.model.spec-xtalk-typed-fixture)]
    [(:ns analysis)
     (count (:specs analysis))
     (count (:functions analysis))])
  => '[hara.model.spec-xtalk-typed-fixture 3 3])

[[:section {:title "Registering and inspecting declarations"}]]

"Parsed declarations can be stored in the global type registry so that later inference can resolve named types and function signatures."

(fact "register and look up declarations"
  (types/clear-registry!)
  (parse/register-types! (parse/analyze-namespace 'hara.model.spec-xtalk-typed-fixture))
  [(-> (types/get-spec 'hara.model.spec-xtalk-typed-fixture/User) :name)
   (-> (types/get-function 'hara.model.spec-xtalk-typed-fixture/find-user) :name)]
  => '["User" "find-user"])

(fact "extract a function type from a registered declaration"
  (types/clear-registry!)
  (parse/register-types! (parse/analyze-namespace 'hara.model.spec-xtalk-typed-fixture))
  (types/type->data
   (types/fn-type
    (types/get-function 'hara.model.spec-xtalk-typed-fixture/find-user)))
  => '{:kind :fn
       :inputs [{:kind :named :name hara.model.spec-xtalk-typed-fixture/UserMap}
                {:kind :primitive :name :xt/str}]
       :output {:kind :maybe :item {:kind :named :name hara.model.spec-xtalk-typed-fixture/User}}})

[[:section {:title "Inferring expression types"}]]

"`infer-type` walks xtalk forms and produces a type result plus any errors. It handles literals, records, field access, and function calls."

(fact "infer literal record and tuple types"
  (types/type->data
   (:type (infer/infer-type '{:id "u1" :tags [1 2]}
                            {:ns 'sample :aliases {} :infer infer/infer-type})))
  => '{:kind :record
       :fields [{:name "id" :type {:kind :primitive :name :xt/str} :optional? false}
                {:name "tags" :type {:kind :tuple :types [{:kind :primitive :name :xt/int}
                                                          {:kind :primitive :name :xt/int}]}
                 :optional? false}]})

(fact "infer field access on a record"
  (types/type->data
   (:type (infer/infer-type '(x:get-key user "id")
                            {:env {'user {:kind :record
                                          :fields [{:name "id" :type types/+str-type+
                                                    :optional? false}]}}
                             :ns 'sample
                             :aliases {}
                             :infer infer/infer-type})))
  => '{:kind :primitive :name :xt/str})

(fact "infer a typed function call"
  (types/clear-registry!)
  (parse/register-types! (parse/analyze-namespace 'hara.model.spec-xtalk-typed-fixture))
  (types/type->data
   (:type (infer/infer-type '(find-user users "u1")
                            {:ns 'hara.model.spec-xtalk-typed-fixture
                             :aliases {}
                             :infer infer/infer-type})))
  => '{:kind :maybe :item {:kind :named :name hara.model.spec-xtalk-typed-fixture/User}})

(fact "detect a type mismatch in a call"
  (types/clear-registry!)
  (parse/register-types! (parse/analyze-namespace 'hara.model.spec-xtalk-typed-fixture))
  (let [result (infer/infer-type '(find-user users 123)
                                 {:ns 'hara.model.spec-xtalk-typed-fixture
                                  :aliases {}
                                  :infer infer/infer-type})]
    [(types/type->data (:type result))
     (-> result :errors first :tag)])
  => '[{:kind :maybe :item {:kind :named :name hara.model.spec-xtalk-typed-fixture/User}}
       :call-arg-type-mismatch])
