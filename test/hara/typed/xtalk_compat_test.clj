(ns hara.typed.xtalk-compat-test
  (:use code.test)
  (:require [hara.typed.xtalk-common :as types]
            [hara.typed.xtalk-compat :refer :all]
            [hara.typed.xtalk-infer :refer [infer-type]]
            [hara.typed.xtalk-parse :as parse]))

(def +ctx+ {:ns 'sample.route :aliases '{k xt.lang.common-lib} :infer infer-type})

(def +user-record+
  {:kind :record
   :fields [{:name "id" :type types/+str-type+ :optional? false}
            {:name "count" :type types/+int-type+ :optional? false}]})

(def +open-record+
  {:kind :record
   :fields [{:name "id" :type types/+str-type+ :optional? false}]
   :open {:key types/+str-type+ :value types/+bool-type+}})

(def +dict-users+ {:kind :dict :key types/+str-type+ :value +user-record+})

(defn fixture-register! []
  (types/clear-registry!)
  (parse/register-types! (parse/analyze-namespace 'hara.model.spec-xtalk-typed-fixture)))

^{:refer hara.typed.xtalk-compat/result :added "4.1"}
(fact "builds result maps"
  (result types/+str-type+ [{:tag :x}] '{v {:kind :primitive :name :xt/int}})
  => '{:type {:kind :primitive :name :xt/str}
       :errors [{:tag :x}]
       :env {v {:kind :primitive :name :xt/int}}})

^{:refer hara.typed.xtalk-compat/with-loc :added "4.1"}
(fact "attaches source locations to inference errors"
  (with-loc (result types/+str-type+ [{:tag :x}]) {:file "sample.clj" :line 5 :column 6})
  => '{:type {:kind :primitive :name :xt/str}
       :errors [{:tag :x :loc {:file "sample.clj" :line 5 :column 6}}]})

^{:refer hara.typed.xtalk-compat/merge-errors :added "4.1"}
(fact "merges error lists from inference results"
  (merge-errors (result types/+str-type+ [{:tag :a}])
                (result types/+int-type+ [{:tag :b}]))
  => '[{:tag :a} {:tag :b}])

^{:refer hara.typed.xtalk-compat/resolve-local-symbol :added "4.1"}
(fact "resolves local and aliased symbols"
  [(resolve-local-symbol 'x:get-key +ctx+)
   (resolve-local-symbol 'k/get-key +ctx+)
   (resolve-local-symbol '-/route +ctx+)
   (resolve-local-symbol 'User +ctx+)]
  => '[x:get-key xt.lang.common-lib/get-key sample.route/route sample.route/User])

^{:refer hara.typed.xtalk-compat/resolve-type :added "4.1"}
(fact "resolves named specs from the registry"
  (do
    (fixture-register!)
    (types/type->data (resolve-type {:kind :named :name 'hara.model.spec-xtalk-typed-fixture/User} +ctx+)))
  => '{:kind :record
       :fields [{:name "id" :type {:kind :primitive :name :xt/str} :optional? false}
                {:name "name" :type {:kind :primitive :name :xt/str} :optional? false}]})

^{:refer hara.typed.xtalk-compat/type-eq? :added "4.1"}
(fact "compares types structurally"
  [(type-eq? types/+str-type+ types/+str-type+)
   (type-eq? types/+str-type+ types/+int-type+)]
  => [true false])

^{:refer hara.typed.xtalk-compat/any-type? :added "4.1"}
(fact "recognizes xt any types"
  [(any-type? {:kind :primitive :name :xt/any})
   (any-type? types/+unknown-type+)]
  => [true false])

^{:refer hara.typed.xtalk-compat/fn-input-compatible? :added "4.1"}
(fact "treats any and unknown inputs permissively"
  [(fn-input-compatible? types/+unknown-type+ types/+str-type+ +ctx+)
   (fn-input-compatible? {:kind :primitive :name :xt/any} types/+str-type+ +ctx+)
   (fn-input-compatible? types/+str-type+ types/+int-type+ +ctx+)]
  => [true true false])

^{:refer hara.typed.xtalk-compat/compatible-type? :added "4.1"}
(fact "checks structural type compatibility"
  [(compatible-type? types/+int-type+ types/+num-type+ +ctx+)
   (compatible-type? {:kind :tuple :types [types/+str-type+]} {:kind :array :item types/+str-type+} +ctx+)
   (compatible-type? {:kind :maybe :item types/+str-type+} types/+str-type+ +ctx+)]
  => [true true false])

^{:refer hara.typed.xtalk-compat/literal-type :added "4.1"}
(fact "infers literal types"
  [(literal-type 1)
   (literal-type '{:id "u1" tag true})
   (literal-type [])]
  => '[{:kind :primitive :name :xt/int}
        {:kind :record
         :fields [{:name "id" :type {:kind :primitive :name :xt/str} :optional? false}]
         :open {:key {:kind :primitive :name :xt/unknown}
                :value {:kind :primitive :name :xt/bool}}}
        {:kind :array :item {:kind :primitive :name :xt/unknown}}])

^{:refer hara.typed.xtalk-compat/field-literal :added "4.1"}
(fact "normalizes literal field keys"
  [(field-literal :route-path)
   (field-literal "listener-id")
   (field-literal 1)]
  => ["route_path" "listener_id" nil])

^{:refer hara.typed.xtalk-compat/literal-key-type :added "4.1"}
(fact "infers key types for literal keys"
  [(literal-key-type :route)
   (literal-key-type "route")
   (literal-key-type 1)]
  => '[{:kind :primitive :name :xt/kw}
        {:kind :primitive :name :xt/str}
        {:kind :primitive :name :xt/unknown}])

^{:refer hara.typed.xtalk-compat/field-access-type :added "4.1"}
(fact "reads field types from records and dicts"
  [(types/type->data (field-access-type +open-record+ "id" +ctx+))
   (types/type->data (field-access-type +open-record+ "flag" +ctx+))
   (types/type->data (field-access-type +dict-users+ "u1" +ctx+))]
  => '[{:kind :primitive :name :xt/str}
        {:kind :maybe :item {:kind :primitive :name :xt/bool}}
        {:kind :maybe :item {:kind :record :fields [{:name "id" :type {:kind :primitive :name :xt/str} :optional? false}
                                                    {:name "count" :type {:kind :primitive :name :xt/int} :optional? false}]}}])

^{:refer hara.typed.xtalk-compat/object-value-type :added "4.1"}
(fact "collects possible object value types"
  [(types/type->data (object-value-type +user-record+ +ctx+))
   (types/type->data (object-value-type +dict-users+ +ctx+))]
  => '[{:kind :union
         :types [{:kind :primitive :name :xt/str}
                 {:kind :primitive :name :xt/int}]}
        {:kind :record
         :fields [{:name "id" :type {:kind :primitive :name :xt/str} :optional? false}
                  {:name "count" :type {:kind :primitive :name :xt/int} :optional? false}]}])
