(ns hara.typed.xtalk-form-test
  (:use code.test)
  (:require [hara.typed.xtalk-common :as types]
            [hara.typed.xtalk-form :refer :all]
            [hara.typed.xtalk-infer :refer [infer-type]]))

(def +ctx+ {:ns 'sample.route :aliases '{k xt.lang.common-lib} :infer infer-type})

^{:refer hara.typed.xtalk-form/infer-map :added "4.1"}
(fact "infers record types from maps"
  (types/type->data (:type (infer-map '{:id "u1" tag true} {:env '{tag {:kind :primitive :name :xt/str}} :ns 'sample.route :aliases {} :infer infer-type})))
  => '{:kind :record
       :fields [{:name "id" :type {:kind :primitive :name :xt/str} :optional? false}]
       :open {:key {:kind :primitive :name :xt/str}
              :value {:kind :primitive :name :xt/bool}}})

^{:refer hara.typed.xtalk-form/infer-vector :added "4.1"}
(fact "infers tuple types from vectors"
  (types/type->data (:type (infer-vector '["u1" 1] +ctx+)))
  => '{:kind :tuple
       :types [{:kind :primitive :name :xt/str}
               {:kind :primitive :name :xt/int}]})

^{:refer hara.typed.xtalk-form/infer-binding-form :added "4.1"}
(fact "infers binding forms and updates env"
  (infer-binding-form '(:= [a b] ["u1" 1]) +ctx+)
  => '{:type {:kind :tuple
              :types [{:kind :primitive :name :xt/str}
                      {:kind :primitive :name :xt/int}]}
       :errors []
       :env {a {:kind :primitive :name :xt/str}
             b {:kind :primitive :name :xt/int}}})

^{:refer hara.typed.xtalk-form/infer-let :added "4.1"}
(fact "infers let bodies with bound symbols"
  (types/type->data (:type (infer-let '(let [(:xt/int count) 1]
                                         count)
                                      +ctx+)))
  => '{:kind :primitive :name :xt/int})

^{:refer hara.typed.xtalk-form/infer-if :added "4.1"}
(fact "unions then and else branches"
  (types/type->data (:type (infer-if '(if true "a" 1) +ctx+)))
  => '{:kind :union
       :types [{:kind :primitive :name :xt/str}
               {:kind :primitive :name :xt/int}]})

^{:refer hara.typed.xtalk-form/infer-ternary :added "4.1"}
(fact "treats ternary as if"
  (types/type->data (:type (infer-ternary '(:? true "a" 1) +ctx+)))
  => '{:kind :union
       :types [{:kind :primitive :name :xt/str}
               {:kind :primitive :name :xt/int}]})

^{:refer hara.typed.xtalk-form/infer-or :added "4.1"}
(fact "strips nil-like members from or results"
  (types/type->data (:type (infer-or '(or maybe-id "guest") {:env '{maybe-id {:kind :maybe :item {:kind :primitive :name :xt/str}}} :ns 'sample.route :aliases {} :infer infer-type})))
  => '{:kind :primitive :name :xt/str})

^{:refer hara.typed.xtalk-form/infer-when :added "4.1"}
(fact "returns nil-or-body for when"
  (types/type->data (:type (infer-when '(when true 1) +ctx+)))
  => '{:kind :union
       :types [{:kind :primitive :name :xt/nil}
               {:kind :primitive :name :xt/int}]})

^{:refer hara.typed.xtalk-form/infer-cond :added "4.1"}
(fact "unions cond branch outputs"
  (types/type->data (:type (infer-cond '(cond flag "a" :else 1) {:env '{flag {:kind :primitive :name :xt/bool}} :ns 'sample.route :aliases {} :infer infer-type})))
  => '{:kind :union
       :types [{:kind :primitive :name :xt/str}
               {:kind :primitive :name :xt/int}]})

^{:refer hara.typed.xtalk-form/infer-while :added "4.1"}
(fact "models while as nil-returning"
  (types/type->data (:type (infer-while '(while true 1) +ctx+)))
  => '{:kind :primitive :name :xt/nil})

^{:refer hara.typed.xtalk-form/infer-yield :added "4.1"}
(fact "models yield as nil-returning"
  (types/type->data (:type (infer-yield '(yield "a") +ctx+)))
  => '{:kind :primitive :name :xt/nil})

^{:refer hara.typed.xtalk-form/infer-anon-fn :added "4.1"}
(fact "infers anonymous fn shapes"
  (types/type->data (:type (infer-anon-fn '(fn [x] x) +ctx+)))
  => '{:kind :fn
       :inputs [{:kind :primitive :name :xt/unknown}]
       :output {:kind :primitive :name :xt/unknown}})

^{:refer hara.typed.xtalk-form/merge-record-fields :added "4.1"}
(fact "merges record fields by name"
  (vec (merge-record-fields [{:name "id" :type types/+str-type+ :optional? false}]
                            [{:name "count" :type types/+int-type+ :optional? false}]))
  => '[{:name "id" :type {:kind :primitive :name :xt/str} :optional? false}
        {:name "count" :type {:kind :primitive :name :xt/int} :optional? false}])

^{:refer hara.typed.xtalk-form/merge-open-types :added "4.1"}
(fact "merges open dict-like types"
  (merge-open-types {:key types/+str-type+ :value types/+bool-type+}
                    {:key types/+str-type+ :value types/+int-type+})
  => '{:key {:kind :primitive :name :xt/str}
       :value {:kind :union
               :types [{:kind :primitive :name :xt/bool}
                       {:kind :primitive :name :xt/int}]}})

^{:refer hara.typed.xtalk-form/infer-obj-assign :added "4.1"}
(fact "merges record and dict object assignments"
  (types/type->data (:type (infer-obj-assign '(x:obj-assign {:id "u1"} {:count 1}) +ctx+)))
  => '{:kind :record
       :fields [{:name "id" :type {:kind :primitive :name :xt/str} :optional? false}
                {:name "count" :type {:kind :primitive :name :xt/int} :optional? false}]})

^{:refer hara.typed.xtalk-form/arrayify-type :added "4.1"}
(fact "converts items and unions to arrays"
  [(types/type->data (arrayify-type types/+str-type+ +ctx+))
   (types/type->data (arrayify-type {:kind :union :types [{:kind :array :item types/+str-type+} types/+int-type+]} +ctx+))]
  => '[{:kind :array :item {:kind :primitive :name :xt/str}}
        {:kind :array :item {:kind :union :types [{:kind :primitive :name :xt/str}
                                                  {:kind :primitive :name :xt/int}]}}])

^{:refer hara.typed.xtalk-form/infer-make-container :added "4.1"}
(fact "infers make-container records"
  (types/type->data (:type (infer-make-container '(xt.event.base-listener/make-container initializer "route" {:tag true})
                                                 {:env '{initializer {:kind :fn :inputs [] :output {:kind :primitive :name :xt/int}}}
                                                  :ns 'sample.route :aliases {} :infer infer-type})))
  => '{:kind :record
       :fields [{:name "::" :type {:kind :primitive :name :xt/str} :optional? false}
                {:name "listeners" :type {:kind :named :name xt.event.base-listener/EventListenerMap} :optional? false}
                {:name "data" :type {:kind :primitive :name :xt/int} :optional? false}
                {:name "initial" :type {:kind :fn :inputs [] :output {:kind :primitive :name :xt/int}} :optional? false}
                {:name "tag" :type {:kind :primitive :name :xt/bool} :optional? false}]})

^{:refer hara.typed.xtalk-form/infer-blank-container :added "4.1"}
(fact "infers blank-container records"
  (types/type->data (:type (infer-blank-container '(xt.event.base-listener/blank-container "route" {:tag true}) +ctx+)))
  => '{:kind :record
       :fields [{:name "::" :type {:kind :primitive :name :xt/str} :optional? false}
                {:name "listeners" :type {:kind :named :name xt.event.base-listener/EventListenerMap} :optional? false}
                {:name "tag" :type {:kind :primitive :name :xt/bool} :optional? false}]})

^{:refer hara.typed.xtalk-form/apply-default-type :added "4.1"}
(fact "adds default values to maybe lookups"
  (types/type->data (apply-default-type {:kind :maybe :item types/+str-type+} types/+int-type+))
  => '{:kind :union
       :types [{:kind :primitive :name :xt/str}
               {:kind :primitive :name :xt/int}]})

^{:refer hara.typed.xtalk-form/infer-fixed-output :added "4.1"}
(fact "builds fixed-output builtin inferencers"
  (let [f (infer-fixed-output types/+bool-type+)]
    (f '(x:nil? :bad) +ctx+))
  => '{:type {:kind :primitive :name :xt/bool}
       :errors []})

^{:refer hara.typed.xtalk-form/infer-obj-vals :added "4.1"}
(fact "infers obj-vals arrays"
  (types/type->data (:type (infer-obj-vals '(x:obj-vals user) {:env '{user {:kind :record :fields [{:name "id" :type {:kind :primitive :name :xt/str} :optional? false}
                                                                                                     {:name "count" :type {:kind :primitive :name :xt/int} :optional? false}]}}
                                                         :ns 'sample.route :aliases {} :infer infer-type})))
  => '{:kind :array
       :item {:kind :union
              :types [{:kind :primitive :name :xt/str}
                      {:kind :primitive :name :xt/int}]}})

^{:refer hara.typed.xtalk-form/infer-obj-pairs :added "4.1"}
(fact "infers obj-pairs arrays"
  (types/type->data (:type (infer-obj-pairs '(x:obj-pairs user) {:env '{user {:kind :record :fields [{:name "id" :type {:kind :primitive :name :xt/str} :optional? false}
                                                                                                       {:name "count" :type {:kind :primitive :name :xt/int} :optional? false}]}}
                                                           :ns 'sample.route :aliases {} :infer infer-type})))
  => '{:kind :array
       :item {:kind :tuple
              :types [{:kind :primitive :name :xt/str}
                      {:kind :union
                       :types [{:kind :primitive :name :xt/str}
                               {:kind :primitive :name :xt/int}]}]}})

^{:refer hara.typed.xtalk-form/infer-obj-clone :added "4.1"}
(fact "clones record-shaped objects"
  (types/type->data (:type (infer-obj-clone '(x:obj-clone user) {:env '{user {:kind :record :fields [{:name "id" :type {:kind :primitive :name :xt/str} :optional? false}]}}
                                                           :ns 'sample.route :aliases {} :infer infer-type})))
  => '{:kind :record
       :fields [{:name "id" :type {:kind :primitive :name :xt/str} :optional? false}]})

^{:refer hara.typed.xtalk-form/infer-arr-clone :added "4.1"}
(fact "clones tuple arrays into array unions"
  (types/type->data (:type (infer-arr-clone '(x:arr-clone values) {:env '{values {:kind :tuple :types [{:kind :primitive :name :xt/str}
                                                                                                          {:kind :primitive :name :xt/int}]}}
                                                           :ns 'sample.route :aliases {} :infer infer-type})))
  => '{:kind :array
       :item {:kind :union
              :types [{:kind :primitive :name :xt/str}
                      {:kind :primitive :name :xt/int}]}})

^{:refer hara.typed.xtalk-form/infer-get-key :added "4.1"}
(fact "infers keyed access with defaults"
  (types/type->data (:type (infer-get-key '(x:get-key users "u1" "guest") {:env '{users {:kind :dict :key {:kind :primitive :name :xt/str} :value {:kind :primitive :name :xt/str}}}
                                                     :ns 'sample.route :aliases {} :infer infer-type})))
  => '{:kind :primitive :name :xt/str})

^{:refer hara.typed.xtalk-form/infer-get-idx :added "4.1"}
(fact "infers indexed access with defaults"
  (types/type->data (:type (infer-get-idx '(x:get-idx values 0 "guest") {:env '{values {:kind :array :item {:kind :primitive :name :xt/str}}}
                                                     :ns 'sample.route :aliases {} :infer infer-type})))
  => '{:kind :primitive :name :xt/str})

^{:refer hara.typed.xtalk-form/infer-get-path :added "4.1"}
(fact "infers path access through nested records"
  (types/type->data (:type (infer-get-path '(x:get-path route ["user" "id"] nil)
                                           {:env '{route {:kind :record
                                                          :fields [{:name "user"
                                                                    :type {:kind :record
                                                                           :fields [{:name "id" :type {:kind :primitive :name :xt/str} :optional? false}]}
                                                                    :optional? false}]}}
                                            :ns 'sample.route :aliases {} :infer infer-type})))
  => '{:kind :primitive :name :xt/str})

^{:refer hara.typed.xtalk-form/infer-dot :added "4.1"}
(fact "dispatches dot access by key or path"
  [(types/type->data (:type (infer-dot '(. route "id") {:env '{route {:kind :record :fields [{:name "id" :type {:kind :primitive :name :xt/str} :optional? false}]}}
                                                   :ns 'sample.route :aliases {} :infer infer-type})))
   (types/type->data (:type (infer-dot '(. route ["id"]) {:env '{route {:kind :record :fields [{:name "id" :type {:kind :primitive :name :xt/str} :optional? false}]}}
                                                     :ns 'sample.route :aliases {} :infer infer-type})))]
  => '[{:kind :primitive :name :xt/str}
        {:kind :primitive :name :xt/str}])

^{:refer hara.typed.xtalk-form/infer-free :added "4.1"}
(fact "treats free forms as wrapped literals"
  [(types/type->data (:type (infer-free '(:- "ok") +ctx+)))
   (types/type->data (:type (infer-free '(:-) +ctx+)))]
  => '[{:kind :primitive :name :xt/str}
        {:kind :primitive :name :xt/unknown}])

^{:refer hara.typed.xtalk-form/infer-keyword-call :added "4.1"}
(fact "treats keyword calls as keyed access"
  (types/type->data (:type (infer-keyword-call '(:entry opts) {:env '{opts {:kind :record :fields [{:name "entry" :type {:kind :primitive :name :xt/str} :optional? false}]}}
                                                         :ns 'sample.route :aliases {} :infer infer-type})))
  => '{:kind :primitive :name :xt/str})

^{:refer hara.typed.xtalk-form/infer-body :added "4.1"}
(fact "returns last body type and threaded env"
  (infer-body '[(:= [a b] ["u1" 1]) a] +ctx+)
  => '{:type {:kind :primitive :name :xt/str}
       :errors []
       :env {a {:kind :primitive :name :xt/str}
             b {:kind :primitive :name :xt/int}}})
