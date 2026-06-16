(ns hara.typed.xtalk-env-test
  (:use code.test)
  (:require [hara.typed.xtalk-common :as types]
            [hara.typed.xtalk-env :refer :all]
            [hara.typed.xtalk-infer :refer [infer-type]]
            [hara.typed.xtalk-parse :as parse]))

(def +ctx+ {:ns 'sample.route :aliases '{k xt.lang.common-lib} :infer infer-type})

(def +user-record+
  {:kind :record
   :fields [{:name "id" :type types/+str-type+ :optional? false}
            {:name "count" :type types/+int-type+ :optional? false}]})

(defn fixture-register! []
  (types/clear-registry!)
  (parse/register-types! (parse/analyze-namespace 'hara.model.spec-xtalk-typed-fixture)))

^{:refer hara.typed.xtalk-env/maybe-register-function! :added "4.1"}
(fact "registers functions lazily from namespaces"
  (do
    (types/clear-registry!)
    (:name (maybe-register-function! 'hara.model.spec-xtalk-typed-fixture/find-user)))
  => "find-user")

^{:refer hara.typed.xtalk-env/lookup-symbol-type :added "4.1"}
(fact "looks up env or registered symbol types"
  (do
    (fixture-register!)
    [(lookup-symbol-type 'user-id {:env '{user-id {:kind :primitive :name :xt/str}} :ns 'sample.route :aliases {} :infer infer-type})
     (types/type->data (lookup-symbol-type 'x:add +ctx+))
     (types/type->data (lookup-symbol-type 'hara.model.spec-xtalk-typed-fixture/find-user +ctx+))])
  => '[{:kind :primitive :name :xt/str}
        {:kind :fn
         :inputs [{:kind :primitive :name :xt/num}
                  {:kind :primitive :name :xt/num}]
         :output {:kind :primitive :name :xt/num}}
        {:kind :fn
         :inputs [{:kind :named :name hara.model.spec-xtalk-typed-fixture/UserMap}
                  {:kind :primitive :name :xt/str}]
         :output {:kind :maybe :item {:kind :named :name hara.model.spec-xtalk-typed-fixture/User}}}])

^{:refer hara.typed.xtalk-env/binding-decl :added "4.1"}
(fact "extracts binding declarations"
  [(binding-decl 'value +ctx+)
   (binding-decl '(:xt/int count) +ctx+)]
  => '[{:symbol value :type nil}
        {:symbol count :type {:kind :primitive :name :xt/int}}])

^{:refer hara.typed.xtalk-env/dynamic-assignment-target? :added "4.1"}
(fact "recognizes dynamic assignment targets"
  [(dynamic-assignment-target? '(x:get-key acc tag))
   (dynamic-assignment-target? 'count)]
  => [true false])

^{:refer hara.typed.xtalk-env/map-binding-updates :added "4.1"}
(fact "builds env updates for map destructuring"
  (map-binding-updates '{:keys [id] :strs [name] :syms [flag]}
                       {:kind :record
                        :fields [{:name "id" :type types/+str-type+ :optional? false}
                                 {:name "name" :type types/+str-type+ :optional? false}
                                 {:name "flag" :type types/+bool-type+ :optional? false}]}
                       +ctx+)
  => '{id {:kind :primitive :name :xt/str}
       name {:kind :primitive :name :xt/str}
       flag {:kind :primitive :name :xt/bool}})

^{:refer hara.typed.xtalk-env/binding-updates :added "4.1"}
(fact "builds env updates for binding targets"
  [(binding-updates '[a b] {:kind :tuple :types [types/+str-type+ types/+int-type+]} +ctx+)
   (binding-updates '#{id} +user-record+ +ctx+)]
  => '[{a {:kind :primitive :name :xt/str}
         b {:kind :primitive :name :xt/int}}
        {id {:kind :primitive :name :xt/str}}])
