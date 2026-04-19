(ns std.lang.typed.xtalk-common-test
  (:use code.test)
  (:require [std.lang.typed.xtalk-common :refer :all]))

(defn sample-spec []
  (make-spec-def 'sample.route 'User +str-type+ {:doc "user"}))

(defn sample-fn []
  (make-fn-def 'sample.route 'find-user [(make-arg 'id +str-type+ [])] +str-type+ {} ['id] nil))

(defn sample-macro []
  (make-fn-def 'sample.route 'expand [(make-arg 'x +unknown-type+ [])] +unknown-type+ {:macro true} ['x] nil))

(defn sample-generator []
  (make-fn-def 'sample.route 'stream [] +unknown-type+ {:generator true} [] nil))

(defn sample-value []
  (make-value-def 'sample.route 'ScopeMap +int-type+ {:def true} {:a 1} nil))

(defn populate! []
  (clear-registry!)
  (register-spec! 'sample.route/User (sample-spec))
  (register-function! 'sample.route/find-user (sample-fn))
  (register-macro! 'sample.route/expand (sample-macro))
  (register-value! 'sample.route/ScopeMap (sample-value)))

^{:refer std.lang.typed.xtalk-common/primitive-type :added "4.1"}
(fact "builds primitive type maps"
  (primitive-type :xt/str)
  => '{:kind :primitive :name :xt/str})

^{:refer std.lang.typed.xtalk-common/snake-case-string :added "4.1"}
(fact "normalizes dash names to snake case"
  (snake-case-string "route-path")
  => "route_path")

^{:refer std.lang.typed.xtalk-common/maybe-type :added "4.1"}
(fact "wraps maybe types"
  (maybe-type +str-type+)
  => '{:kind :maybe :item {:kind :primitive :name :xt/str}})

^{:refer std.lang.typed.xtalk-common/union-type :added "4.1"}
(fact "dedupes union members"
  (union-type [+str-type+ +str-type+ +int-type+])
  => '{:kind :union :types [{:kind :primitive :name :xt/str}
                            {:kind :primitive :name :xt/int}]})

^{:refer std.lang.typed.xtalk-common/type-key :added "4.1"}
(fact "builds namespaced type keys"
  [(type-key 'sample.route 'User)
   (type-key nil 'User)]
  => '[sample.route/User User])

^{:refer std.lang.typed.xtalk-common/valid-key? :added "4.1"}
(fact "accepts only namespaced symbols as registry keys"
  [(valid-key? 'sample.route/User)
   (valid-key? 'User)]
  => [true false])

^{:refer std.lang.typed.xtalk-common/clear-registry! :added "4.1"}
(fact "clears registry state"
  (do (populate!) (clear-registry!) (count (list-entries)))
  => 0)

^{:refer std.lang.typed.xtalk-common/make-registry-entry :added "4.1"}
(fact "creates blank registry entries"
  (make-registry-entry 'sample.route/User)
  => '{:symbol sample.route/User :spec nil :fn nil :macro nil :value nil})

^{:refer std.lang.typed.xtalk-common/get-entry :added "4.1"}
(fact "gets registry entries"
  (do (populate!) (some? (get-entry 'sample.route/User)))
  => true)

^{:refer std.lang.typed.xtalk-common/entry-declarations :added "4.1"}
(fact "extracts present declarations from an entry"
  (do (populate!) (keys (entry-declarations (get-entry 'sample.route/User))))
  => '(:spec))

^{:refer std.lang.typed.xtalk-common/entry-kinds :added "4.1"}
(fact "lists declaration kinds"
  (do (populate!) (vec (entry-kinds (get-entry 'sample.route/find-user))))
  => [:fn])

^{:refer std.lang.typed.xtalk-common/entry-primary :added "4.1"}
(fact "returns primary entry declaration"
  (do (populate!) (:name (entry-primary (get-entry 'sample.route/User))))
  => "User")

^{:refer std.lang.typed.xtalk-common/entry-primary-kind :added "4.1"}
(fact "returns primary declaration kind"
  (do (populate!) (entry-primary-kind (get-entry 'sample.route/ScopeMap)))
  => :value)

^{:refer std.lang.typed.xtalk-common/get-declaration :added "4.1"}
(fact "gets specific declarations by kind"
  (do (populate!) (:name (get-declaration 'sample.route/find-user :fn)))
  => "find-user")

^{:refer std.lang.typed.xtalk-common/get-spec :added "4.1"}
(fact "gets registered specs"
  (do (populate!) (:name (get-spec 'sample.route/User)))
  => "User")

^{:refer std.lang.typed.xtalk-common/get-function :added "4.1"}
(fact "gets registered functions"
  (do (populate!) (:name (get-function 'sample.route/find-user)))
  => "find-user")

^{:refer std.lang.typed.xtalk-common/get-macro :added "4.1"}
(fact "gets registered macros"
  (do (populate!) (:name (get-macro 'sample.route/expand)))
  => "expand")

^{:refer std.lang.typed.xtalk-common/get-value :added "4.1"}
(fact "gets registered values"
  (do (populate!) (:name (get-value 'sample.route/ScopeMap)))
  => "ScopeMap")

^{:refer std.lang.typed.xtalk-common/get-type :added "4.1"}
(fact "gets primary typed declarations"
  (do (populate!) (:name (get-type 'sample.route/find-user)))
  => "find-user")

^{:refer std.lang.typed.xtalk-common/list-specs :added "4.1"}
(fact "lists specs"
  (do (populate!) (count (list-specs)))
  => 1)

^{:refer std.lang.typed.xtalk-common/list-functions :added "4.1"}
(fact "lists functions"
  (do (populate!) (count (list-functions)))
  => 1)

^{:refer std.lang.typed.xtalk-common/list-macros :added "4.1"}
(fact "lists macros"
  (do (populate!) (count (list-macros)))
  => 1)

^{:refer std.lang.typed.xtalk-common/list-values :added "4.1"}
(fact "lists values"
  (do (populate!) (count (list-values)))
  => 1)

^{:refer std.lang.typed.xtalk-common/list-entries :added "4.1"}
(fact "lists registry entries"
  (do (populate!) (count (list-entries)))
  => 4)

^{:refer std.lang.typed.xtalk-common/register-entry! :added "4.1"}
(fact "register-entry rejects invalid keys"
  (try (register-entry! 'User :spec {})
       false
       (catch clojure.lang.ExceptionInfo ex
         (= 'User (-> ex ex-data :key))))
  => true)

^{:refer std.lang.typed.xtalk-common/register-spec! :added "4.1"}
(fact "registers specs"
  (do (clear-registry!) (register-spec! 'sample.route/User (sample-spec)) (some? (get-spec 'sample.route/User)))
  => true)

^{:refer std.lang.typed.xtalk-common/register-function! :added "4.1"}
(fact "registers functions"
  (do (clear-registry!) (register-function! 'sample.route/find-user (sample-fn)) (some? (get-function 'sample.route/find-user)))
  => true)

^{:refer std.lang.typed.xtalk-common/register-macro! :added "4.1"}
(fact "registers macros"
  (do (clear-registry!) (register-macro! 'sample.route/expand (sample-macro)) (some? (get-macro 'sample.route/expand)))
  => true)

^{:refer std.lang.typed.xtalk-common/register-value! :added "4.1"}
(fact "registers values"
  (do (clear-registry!) (register-value! 'sample.route/ScopeMap (sample-value)) (some? (get-value 'sample.route/ScopeMap)))
  => true)

^{:refer std.lang.typed.xtalk-common/make-spec-def :added "4.1"}
(fact "constructs spec defs"
  (sample-spec)
  => '{:ns "sample.route" :name "User" :type {:kind :primitive :name :xt/str} :spec-meta {:doc "user"}})

^{:refer std.lang.typed.xtalk-common/make-arg :added "4.1"}
(fact "constructs args"
  (make-arg 'id +str-type+ [:rest])
  => '{:name id :type {:kind :primitive :name :xt/str} :modifiers [:rest]})

^{:refer std.lang.typed.xtalk-common/make-fn-def :added "4.1"}
(fact "constructs fn defs"
  (:name (sample-fn))
  => "find-user")

^{:refer std.lang.typed.xtalk-common/make-value-def :added "4.1"}
(fact "constructs value defs"
  (:raw-value (sample-value))
  => {:a 1})

^{:refer std.lang.typed.xtalk-common/spec-def? :added "4.1"}
(fact "detects spec defs"
  (spec-def? (sample-spec))
  => true)

^{:refer std.lang.typed.xtalk-common/fn-def? :added "4.1"}
(fact "detects non-macro fn defs"
  (fn-def? (sample-fn))
  => true)

^{:refer std.lang.typed.xtalk-common/macro-def? :added "4.1"}
(fact "detects macro defs"
  (macro-def? (sample-macro))
  => true)

^{:refer std.lang.typed.xtalk-common/generator-def? :added "4.1"}
(fact "detects generator defs"
  (generator-def? (sample-generator))
  => true)

^{:refer std.lang.typed.xtalk-common/value-def? :added "4.1"}
(fact "detects value defs"
  (value-def? (sample-value))
  => true)

^{:refer std.lang.typed.xtalk-common/declaration-kind :added "4.1"}
(fact "classifies declarations"
  [(declaration-kind (sample-spec))
   (declaration-kind (sample-fn))
   (declaration-kind (sample-macro))
   (declaration-kind (sample-value))]
  => [:spec :fn :macro :value])

^{:refer std.lang.typed.xtalk-common/field-key :added "4.1"}
(fact "normalizes field names"
  [(field-key :route-path)
   (field-key 'event-meta)
   (field-key "listener-id")]
  => ["route_path" "event_meta" "listener_id"])

^{:refer std.lang.typed.xtalk-common/likely-type-symbol? :added "4.1"}
(fact "detects likely type symbols"
  [(likely-type-symbol? 'User)
   (likely-type-symbol? 'sample.route/User)
   (likely-type-symbol? 'user)]
  => [true true false])

^{:refer std.lang.typed.xtalk-common/resolve-type-symbol :added "4.1"}
(fact "resolves aliased and local type symbols"
  [(resolve-type-symbol 'User {:ns 'sample.route :aliases {}})
   (resolve-type-symbol 'types/EventMap {:ns 'sample.route :aliases '{types xt.lang.event-common}})]
  => '[sample.route/User xt.lang.event-common/EventMap])

^{:refer std.lang.typed.xtalk-common/normalize-record-field :added "4.1"}
(fact "normalizes record field definitions"
  (normalize-record-field '["listener-id" [:xt/maybe :xt/str]] {:ns 'sample.route :aliases {}})
  => '{:name "listener_id"
       :type {:kind :maybe :item {:kind :primitive :name :xt/str}}
       :optional? true})

^{:refer std.lang.typed.xtalk-common/normalize-apply-target :added "4.1"}
(fact "normalizes apply targets"
  (normalize-apply-target 'types/EventMap {:ns 'sample.route :aliases '{types xt.lang.event-common}})
  => 'xt.lang.event-common/EventMap)

^{:refer std.lang.typed.xtalk-common/normalize-type :added "4.1"}
(fact "normalizes xt type forms"
  (normalize-type '[:xt/record ["id" :xt/str] ["tags" [:xt/array :xt/str]]] {:ns 'sample.route :aliases {}})
  => '{:kind :record
       :fields [{:name "id" :type {:kind :primitive :name :xt/str} :optional? false}
                {:name "tags" :type {:kind :array :item {:kind :primitive :name :xt/str}} :optional? false}]})

(fact "normalizes xt self from context"
  (normalize-type :xt/self
                  {:ns 'sample.route
                   :aliases {}
                   :self {:kind :array
                          :item {:kind :primitive :name :xt/str}}})
  => '{:kind :array
       :item {:kind :primitive :name :xt/str}})

^{:refer std.lang.typed.xtalk-common/normalize-return-meta :added "4.1"}
(fact "normalizes wrapped return metadata"
  (normalize-return-meta '[[:xt/maybe User]] {:ns 'sample.route :aliases {}})
  => '{:kind :maybe :item {:kind :named :name sample.route/User}})

^{:refer std.lang.typed.xtalk-common/fn-type :added "4.1"}
(fact "extracts fn types from fn defs"
  (fn-type (make-fn-def 'sample.route 'find-user [(make-arg 'id +str-type+ [])] +bool-type+ {} [] nil))
  => '{:kind :fn
       :inputs [{:kind :primitive :name :xt/str}]
       :output {:kind :primitive :name :xt/bool}})

^{:refer std.lang.typed.xtalk-common/type->data :added "4.1"}
(fact "converts typed records to plain data"
  (type->data (sample-spec))
  => '{:ns "sample.route" :name "User" :type {:kind :primitive :name :xt/str}})

^{:refer std.lang.typed.xtalk-common/type-string :added "4.1"}
(fact "renders type data as strings"
  (boolean (re-find #":xt/str" (type-string +str-type+)))
  => true)

^{:refer std.lang.typed.xtalk-common/current-function-symbol :added "4.1"}
(fact "returns current function symbols"
  (current-function-symbol (sample-fn))
  => 'sample.route/find-user)
