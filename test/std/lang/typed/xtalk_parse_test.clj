(ns std.lang.typed.xtalk-parse-test
  (:use code.test)
  (:require [std.lang.typed.xtalk-common :as types]
            [std.lang.typed.xtalk-parse :refer :all]))

(def +ctx+ {:ns 'sample.route :aliases '{types xt.lang.event-common}})

^{:refer std.lang.typed.xtalk-parse/read-forms :added "4.1"}
(fact "reads forms from files"
  (pos? (count (read-forms "test/std/lang/model/spec_xtalk_typed_fixture.clj")))
  => true)

^{:refer std.lang.typed.xtalk-parse/ns-form? :added "4.1"}
(fact "detects ns forms"
  [(ns-form? '(ns sample.core))
   (ns-form? '(def x 1))]
  => [true false])

^{:refer std.lang.typed.xtalk-parse/defspec? :added "4.1"}
(fact "detects defspec.xt forms"
  [(defspec? '(defspec.xt User :xt/str))
   (defspec? '(defn.xt user []))]
  => [true false])

^{:refer std.lang.typed.xtalk-parse/defn? :added "4.1"}
(fact "detects xt and clojure defn forms"
  [(defn? '(defn.xt f []))
   (defn? '(defgen.xt f []))
   (defn? '(defmacro.xt f []))]
  => ["defn.xt" "defgen.xt" nil])

^{:refer std.lang.typed.xtalk-parse/defmacro? :added "4.1"}
(fact "detects defmacro.xt forms"
  [(defmacro? '(defmacro.xt f []))
   (defmacro? '(defn.xt f []))]
  => [true false])

^{:refer std.lang.typed.xtalk-parse/defvalue? :added "4.1"}
(fact "detects def.xt forms"
  [(defvalue? '(def.xt Value 1))
   (defvalue? '(defn.xt f []))]
  => [true false])

^{:refer std.lang.typed.xtalk-parse/parse-ns-name :added "4.1"}
(fact "extracts namespace names from form streams"
  (parse-ns-name '[(comment x) (ns sample.core) (def x 1)])
  => 'sample.core)

^{:refer std.lang.typed.xtalk-parse/extract-aliases :added "4.1"}
(fact "extracts aliases from require vectors"
  (extract-aliases '[[xt.lang.common-lib :as k]
                     [xt.lang.event-common :as event-common]])
  => '{k xt.lang.common-lib
       event-common xt.lang.event-common})

^{:refer std.lang.typed.xtalk-parse/extract-ns-aliases :added "4.1"}
(fact "extracts aliases from ns forms"
  (extract-ns-aliases '(ns sample.core (:require [xt.lang.common-lib :as k])))
  => '{k xt.lang.common-lib})

^{:refer std.lang.typed.xtalk-parse/script-form? :added "4.1"}
(fact "detects script forms"
  [(script-form? '(script :xtalk {:require []}))
   (script-form? '(ns sample.core))]
  => [true false])

^{:refer std.lang.typed.xtalk-parse/extract-script-aliases :added "4.1"}
(fact "extracts aliases from script forms"
  (extract-script-aliases '[(script :xtalk {:require [[xt.lang.common-lib :as k]]})])
  => '{k xt.lang.common-lib})

^{:refer std.lang.typed.xtalk-parse/arg-from-inline-form :added "4.1"}
(fact "builds args from inline type forms"
  (arg-from-inline-form '(:xt/int id) +ctx+)
  => '{:name id :type {:kind :primitive :name :xt/int} :modifiers []})

^{:refer std.lang.typed.xtalk-parse/arg-declared-type :added "4.1"}
(fact "reads declared arg metadata"
  (arg-declared-type (with-meta 'id {:- :xt/str}) +ctx+)
  => '{:kind :primitive :name :xt/str})

^{:refer std.lang.typed.xtalk-parse/binding-symbols :added "4.1"}
(fact "extracts symbols from destructuring forms"
  (vec (binding-symbols '[user {:keys [id]} & [opts]]))
  => '[user id opts])

^{:refer std.lang.typed.xtalk-parse/parse-fn-inputs :added "4.1"}
(fact "parses typed and destructured fn args"
  (mapv :name (parse-fn-inputs '[UserMap users :xt/int id & [opts]] +ctx+))
  => '[users id opts])

^{:refer std.lang.typed.xtalk-parse/parse-spec-decl :added "4.1"}
(fact "parses spec declarations"
  (-> (parse-spec-decl 'sample.route 'User '[:xt/record ["id" :xt/str]] {:docstring "user"} {}) :type types/type->data)
  => '{:kind :record :fields [{:name "id" :type {:kind :primitive :name :xt/str} :optional? false}]})

^{:refer std.lang.typed.xtalk-parse/parse-decl-preamble :added "4.1"}
(fact "splits docstrings attr maps and remaining items"
  (parse-decl-preamble '("doc" {:added "4.1"} [x] (+ x 1)) 'f)
  => '{:docstring "doc"
       :attr-map {:added "4.1"}
       :items ([x] (+ x 1))
       :meta {:added "4.1" :docstring "doc"}})

^{:refer std.lang.typed.xtalk-parse/parse-defspec :added "4.1"}
(fact "parses defspec forms"
  (:name (parse-defspec '(defspec.xt User :xt/str) 'sample.route {}))
  => "User")

^{:refer std.lang.typed.xtalk-parse/parse-callable-items :added "4.1"}
(fact "parses callable arg/body sections"
  [(parse-callable-items '([x] (+ x 1)))
   (parse-callable-items '((([x] (+ x 1)))))]
  => '[[[x] ((+ x 1))]
       [(([x] (+ x 1))) ()]])

^{:refer std.lang.typed.xtalk-parse/parse-defn :added "4.1"}
(fact "parses defn.xt forms"
  (let [fn-def (parse-defn '(defn.xt ^{:- [:xt/maybe User]} find-user [UserMap users :xt/str id] (return id)) 'sample.route {})]
    [(mapv :name (:inputs fn-def))
     (types/type->data (:output fn-def))])
  => '[[users id]
       {:kind :maybe :item {:kind :named :name sample.route/User}}])

^{:refer std.lang.typed.xtalk-parse/parse-defmacro :added "4.1"}
(fact "parses defmacro.xt forms"
  (get-in (parse-defmacro '(defmacro.xt add [a b] (list '+ a b)) 'sample.route {}) [:body-meta :macro])
  => true)

(fact "parses multi-arity defmacro.xt forms"
  (let [macro-def (parse-defmacro
                   '(defmacro.xt ^{:standalone true}
                      x:get-idx
                      ([arr idx]
                       (list (quote x:get-idx) arr idx))
                      ([arr idx default]
                       (list (quote x:get-idx) arr idx default)))
                   'sample.route
                   {})]
    {:name (:name macro-def)
     :inputs (mapv :name (:inputs macro-def))
     :standalone (get-in macro-def [:body-meta :standalone])
     :raw-body (:raw-body macro-def)})
  => '{:name "x:get-idx"
       :inputs [arr idx]
       :standalone true
       :raw-body [([arr idx]
                   (list (quote x:get-idx) arr idx))
                  ([arr idx default]
                   (list (quote x:get-idx) arr idx default))]})

^{:refer std.lang.typed.xtalk-parse/parse-defvalue :added "4.1"}
(fact "parses def.xt forms"
  (types/type->data (:type (parse-defvalue '(def.xt ^{:- [:xt/dict :xt/str :xt/num]} ScopeMap {:a 1}) 'sample.route {})))
  => '{:kind :dict
       :key {:kind :primitive :name :xt/str}
       :value {:kind :primitive :name :xt/num}})

^{:refer std.lang.typed.xtalk-parse/merge-spec-inputs :added "4.1"}
(fact "fills unknown arg types from spec inputs"
  (mapv (comp types/type->data :type)
        (merge-spec-inputs [(types/make-arg 'id types/+unknown-type+ [])]
                           [types/+str-type+]))
  => '[{:kind :primitive :name :xt/str}])

^{:refer std.lang.typed.xtalk-parse/attach-function-spec :added "4.1"}
(fact "attaches callable specs to fn defs"
  (let [fn-def (types/make-fn-def 'sample.route 'find-user [(types/make-arg 'id types/+unknown-type+ [])] types/+unknown-type+ {} ['id] nil)
        spec (parse-spec-decl 'sample.route 'find-user '[:fn [:xt/str] :xt/bool] {} {})]
    (types/type->data (:output (attach-function-spec fn-def spec))))
  => '{:kind :primitive :name :xt/bool})

^{:refer std.lang.typed.xtalk-parse/attach-value-spec :added "4.1"}
(fact "attaches value specs to value defs"
  (let [value-def (types/make-value-def 'sample.route 'ScopeMap types/+unknown-type+ {} {:a 1} nil)
        spec (parse-spec-decl 'sample.route 'ScopeMap '[:xt/dict :xt/str :xt/int] {} {})]
    (types/type->data (:type (attach-value-spec value-def spec))))
  => '{:kind :dict
       :key {:kind :primitive :name :xt/str}
       :value {:kind :primitive :name :xt/int}})

^{:refer std.lang.typed.xtalk-parse/spec-map-by-kind :added "4.1"}
(fact "indexes filtered specs by type key"
  (keys (spec-map-by-kind [(parse-spec-decl 'sample.route 'find-user '[:fn [:xt/str] :xt/bool] {} {})]
                          #(= :fn (get-in % [:type :kind]))))
  => '(sample.route/find-user))

^{:refer std.lang.typed.xtalk-parse/attach-specs :added "4.1"}
(fact "attaches specs across analysis outputs"
  (let [analysis (analyze-namespace 'std.lang.model.spec-xtalk-typed-fixture)]
    [(some? (:spec (first (:functions analysis))))
     (= 3 (count (:specs analysis)))])
  => '[true true])

^{:refer std.lang.typed.xtalk-parse/analyze-file :added "4.1"}
(fact "analyzes files into typed declarations"
  (:ns (analyze-file "test/std/lang/model/spec_xtalk_typed_fixture.clj"))
  => 'std.lang.model.spec-xtalk-typed-fixture)

^{:refer std.lang.typed.xtalk-parse/register-types! :added "4.1"}
(fact "registers parsed declarations"
  (do
    (types/clear-registry!)
    (register-types! (analyze-namespace 'std.lang.model.spec-xtalk-typed-fixture))
    (some? (types/get-function 'std.lang.model.spec-xtalk-typed-fixture/find-user)))
  => true)

^{:refer std.lang.typed.xtalk-parse/analyze-namespace :added "4.1"}
(fact "finds source files for namespaces"
  (count (:functions (analyze-namespace 'std.lang.model.spec-xtalk-typed-fixture)))
  => 3)


^{:refer std.lang.typed.xtalk-parse/analyze-file-raw :added "4.1"}
(fact "returns raw parsed map without spec attachment"
  (let [result (analyze-file-raw "test/std/lang/model/spec_xtalk_typed_fixture.clj")]
    [(map? result)
     (:ns result)
     (contains? result :specs)
     (contains? result :functions)])
  => [true 'std.lang.model.spec-xtalk-typed-fixture true true])

^{:refer std.lang.typed.xtalk-parse/analyze-namespace-raw :added "4.1"}
(fact "looks up namespace source file and returns raw analysis"
  (let [result (analyze-namespace-raw 'std.lang.model.spec-xtalk-typed-fixture)]
    [(map? result)
     (:ns result)])
  => [true 'std.lang.model.spec-xtalk-typed-fixture])
