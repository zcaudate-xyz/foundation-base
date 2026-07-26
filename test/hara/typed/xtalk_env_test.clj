tahto/typed/xtalk_env_test.clj:1:(ns tahto.typed.xtalk-env-test
  (:use code.test)
tahto/typed/xtalk_env_test.clj:3:  (:require [tahto.typed.xtalk-common :as types]
tahto/typed/xtalk_env_test.clj:4:            [tahto.typed.xtalk-env :refer :all]
tahto/typed/xtalk_env_test.clj:5:            [tahto.typed.xtalk-infer :refer [infer-type]]
tahto/typed/xtalk_env_test.clj:6:            [tahto.typed.xtalk-parse :as parse]))

(def +ctx+ {:ns 'sample.route :aliases '{k xt.lang.common-lib} :infer infer-type})

(def +user-record+
  {:kind :record
   :fields [{:name "id" :type types/+str-type+ :optional? false}
            {:name "count" :type types/+int-type+ :optional? false}]})

(defn fixture-register! []
  (types/clear-registry!)
tahto/typed/xtalk_env_test.clj:17:  (parse/register-types! (parse/analyze-namespace 'tahto.model.spec-xtalk-typed-fixture)))

tahto/typed/xtalk_env_test.clj:19:^{:refer tahto.typed.xtalk-env/maybe-register-function! :added "4.1"}
(fact "registers functions lazily from namespaces"
  (do
    (types/clear-registry!)
tahto/typed/xtalk_env_test.clj:23:    (:name (maybe-register-function! 'tahto.model.spec-xtalk-typed-fixture/find-user)))
  => "find-user")

tahto/typed/xtalk_env_test.clj:26:^{:refer tahto.typed.xtalk-env/lookup-symbol-type :added "4.1"}
(fact "looks up env or registered symbol types"
  (do
    (fixture-register!)
    [(lookup-symbol-type 'user-id {:env '{user-id {:kind :primitive :name :xt/str}} :ns 'sample.route :aliases {} :infer infer-type})
     (types/type->data (lookup-symbol-type 'x:add +ctx+))
tahto/typed/xtalk_env_test.clj:32:     (types/type->data (lookup-symbol-type 'tahto.model.spec-xtalk-typed-fixture/find-user +ctx+))])
  => '[{:kind :primitive :name :xt/str}
        {:kind :fn
         :inputs [{:kind :primitive :name :xt/num}
                  {:kind :primitive :name :xt/num}]
         :output {:kind :primitive :name :xt/num}}
        {:kind :fn
tahto/typed/xtalk_env_test.clj:39:         :inputs [{:kind :named :name tahto.model.spec-xtalk-typed-fixture/UserMap}
                  {:kind :primitive :name :xt/str}]
tahto/typed/xtalk_env_test.clj:41:         :output {:kind :maybe :item {:kind :named :name tahto.model.spec-xtalk-typed-fixture/User}}}])

tahto/typed/xtalk_env_test.clj:43:^{:refer tahto.typed.xtalk-env/binding-decl :added "4.1"}
(fact "extracts binding declarations"
  [(binding-decl 'value +ctx+)
   (binding-decl '(:xt/int count) +ctx+)]
  => '[{:symbol value :type nil}
        {:symbol count :type {:kind :primitive :name :xt/int}}])

tahto/typed/xtalk_env_test.clj:50:^{:refer tahto.typed.xtalk-env/dynamic-assignment-target? :added "4.1"}
(fact "recognizes dynamic assignment targets"
  [(dynamic-assignment-target? '(x:get-key acc tag))
   (dynamic-assignment-target? 'count)]
  => [true false])

tahto/typed/xtalk_env_test.clj:56:^{:refer tahto.typed.xtalk-env/map-binding-updates :added "4.1"}
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

tahto/typed/xtalk_env_test.clj:68:^{:refer tahto.typed.xtalk-env/binding-updates :added "4.1"}
(fact "builds env updates for binding targets"
  [(binding-updates '[a b] {:kind :tuple :types [types/+str-type+ types/+int-type+]} +ctx+)
   (binding-updates '#{id} +user-record+ +ctx+)]
  => '[{a {:kind :primitive :name :xt/str}
         b {:kind :primitive :name :xt/int}}
        {id {:kind :primitive :name :xt/str}}])
