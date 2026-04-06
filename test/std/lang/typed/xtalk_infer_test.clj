(ns std.lang.typed.xtalk-infer-test
  (:use code.test)
  (:require [std.lang.typed.xtalk-common :as types]
            [std.lang.typed.xtalk-infer :refer :all]
            [std.lang.typed.xtalk-ops :as ops]
            [std.lang.typed.xtalk-parse :as parse]))

(def +ctx+ {:ns 'sample.route :aliases '{k xt.lang.base-lib}})
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
  (parse/register-types! (parse/analyze-namespace 'std.lang.model.spec-xtalk-typed-fixture)))

^{:refer std.lang.typed.xtalk-infer/result :added "4.1"}
(fact "builds result maps"
  (result types/+str-type+ [{:tag :x}] '{v {:kind :primitive :name :xt/int}})
  => '{:type {:kind :primitive :name :xt/str}
       :errors [{:tag :x}]
       :env {v {:kind :primitive :name :xt/int}}})

^{:refer std.lang.typed.xtalk-infer/merge-errors :added "4.1"}
(fact "merges error lists from inference results"
  (merge-errors (result types/+str-type+ [{:tag :a}])
                (result types/+int-type+ [{:tag :b}]))
  => '[{:tag :a} {:tag :b}])

^{:refer std.lang.typed.xtalk-infer/resolve-local-symbol :added "4.1"}
(fact "resolves local and aliased symbols"
  [(resolve-local-symbol 'x:get-key +ctx+)
   (resolve-local-symbol 'k/get-key +ctx+)
   (resolve-local-symbol '-/route +ctx+)
   (resolve-local-symbol 'User +ctx+)]
  => '[x:get-key xt.lang.base-lib/get-key sample.route/route sample.route/User])

^{:refer std.lang.typed.xtalk-infer/resolve-type :added "4.1"}
(fact "resolves named specs from the registry"
  (do
    (fixture-register!)
    (types/type->data (resolve-type {:kind :named :name 'std.lang.model.spec-xtalk-typed-fixture/User} +ctx+)))
  => '{:kind :record
       :fields [{:name "id" :type {:kind :primitive :name :xt/str} :optional? false}
                {:name "name" :type {:kind :primitive :name :xt/str} :optional? false}]})

^{:refer std.lang.typed.xtalk-infer/type-eq? :added "4.1"}
(fact "compares types structurally"
  [(type-eq? types/+str-type+ types/+str-type+)
   (type-eq? types/+str-type+ types/+int-type+)]
  => [true false])

^{:refer std.lang.typed.xtalk-infer/any-type? :added "4.1"}
(fact "recognizes xt any types"
  [(any-type? {:kind :primitive :name :xt/any})
   (any-type? types/+unknown-type+)]
  => [true false])

^{:refer std.lang.typed.xtalk-infer/fn-input-compatible? :added "4.1"}
(fact "treats any and unknown inputs permissively"
  [(fn-input-compatible? types/+unknown-type+ types/+str-type+ +ctx+)
   (fn-input-compatible? {:kind :primitive :name :xt/any} types/+str-type+ +ctx+)
   (fn-input-compatible? types/+str-type+ types/+int-type+ +ctx+)]
  => [true true false])

^{:refer std.lang.typed.xtalk-infer/compatible-type? :added "4.1"}
(fact "checks structural type compatibility"
  [(compatible-type? types/+int-type+ types/+num-type+ +ctx+)
   (compatible-type? {:kind :tuple :types [types/+str-type+]} {:kind :array :item types/+str-type+} +ctx+)
   (compatible-type? {:kind :maybe :item types/+str-type+} types/+str-type+ +ctx+)]
  => [true true false])

^{:refer std.lang.typed.xtalk-infer/literal-type :added "4.1"}
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

^{:refer std.lang.typed.xtalk-infer/field-literal :added "4.1"}
(fact "normalizes literal field keys"
  [(field-literal :route-path)
   (field-literal "listener-id")
   (field-literal 1)]
  => ["route_path" "listener_id" nil])

^{:refer std.lang.typed.xtalk-infer/literal-key-type :added "4.1"}
(fact "infers key types for literal keys"
  [(literal-key-type :route)
   (literal-key-type "route")
   (literal-key-type 1)]
  => '[{:kind :primitive :name :xt/kw}
        {:kind :primitive :name :xt/str}
        {:kind :primitive :name :xt/unknown}])

^{:refer std.lang.typed.xtalk-infer/maybe-register-function! :added "4.1"}
(fact "registers functions lazily from namespaces"
  (do
    (types/clear-registry!)
    (:name (maybe-register-function! 'std.lang.model.spec-xtalk-typed-fixture/find-user)))
  => "find-user")

^{:refer std.lang.typed.xtalk-infer/lookup-symbol-type :added "4.1"}
(fact "looks up env or registered symbol types"
  (do
    (fixture-register!)
    [(lookup-symbol-type 'user-id {:env '{user-id {:kind :primitive :name :xt/str}} :ns 'sample.route :aliases {}})
     (types/type->data (lookup-symbol-type 'x:add +ctx+))
     (types/type->data (lookup-symbol-type 'std.lang.model.spec-xtalk-typed-fixture/find-user +ctx+))])
  => '[{:kind :primitive :name :xt/str}
        {:kind :fn
         :inputs [{:kind :primitive :name :xt/num}
                  {:kind :primitive :name :xt/num}]
         :output {:kind :primitive :name :xt/num}}
        {:kind :fn
         :inputs [{:kind :named :name std.lang.model.spec-xtalk-typed-fixture/UserMap}
                  {:kind :primitive :name :xt/str}]
         :output {:kind :maybe :item {:kind :named :name std.lang.model.spec-xtalk-typed-fixture/User}}}])

^{:refer std.lang.typed.xtalk-infer/infer-op-spec-form :added "4.1"}
(fact "infers builtin calls directly from op-spec declarations"
  [(types/type->data (:type (infer-op-spec-form (ops/canonical-entry 'x:add)
                                                '(x:add 1 2)
                                                +ctx+)))
   (:errors (infer-op-spec-form (ops/canonical-entry 'x:add)
                                '(x:add 1 "b")
                                +ctx+))
   (:tag (first (:errors (infer-op-spec-form (ops/canonical-entry 'x:add)
                                             '(x:add 1)
                                             +ctx+))))]
  => '[{:kind :primitive :name :xt/num}
        [{:tag :call-arg-type-mismatch
          :form "b"
          :expected {:kind :primitive :name :xt/num}
          :actual {:kind :primitive :name :xt/str}}]
        :call-arity-mismatch])

^{:refer std.lang.typed.xtalk-infer/field-access-type :added "4.1"}
(fact "reads field types from records and dicts"
  [(types/type->data (field-access-type +open-record+ "id" +ctx+))
   (types/type->data (field-access-type +open-record+ "flag" +ctx+))
   (types/type->data (field-access-type +dict-users+ "u1" +ctx+))]
  => '[{:kind :primitive :name :xt/str}
        {:kind :maybe :item {:kind :primitive :name :xt/bool}}
        {:kind :maybe :item {:kind :record :fields [{:name "id" :type {:kind :primitive :name :xt/str} :optional? false}
                                                    {:name "count" :type {:kind :primitive :name :xt/int} :optional? false}]}}])

^{:refer std.lang.typed.xtalk-infer/binding-decl :added "4.1"}
(fact "extracts binding declarations"
  [(binding-decl 'value +ctx+)
   (binding-decl '(:xt/int count) +ctx+)]
  => '[{:symbol value :type nil}
        {:symbol count :type {:kind :primitive :name :xt/int}}])

^{:refer std.lang.typed.xtalk-infer/dynamic-assignment-target? :added "4.1"}
(fact "recognizes dynamic assignment targets"
  [(dynamic-assignment-target? '(x:get-key acc tag))
   (dynamic-assignment-target? 'count)]
  => [true false])

^{:refer std.lang.typed.xtalk-infer/infer-map :added "4.1"}
(fact "infers record types from maps"
  (types/type->data (:type (infer-map '{:id "u1" tag true} {:env '{tag {:kind :primitive :name :xt/str}} :ns 'sample.route :aliases {}})))
  => '{:kind :record
       :fields [{:name "id" :type {:kind :primitive :name :xt/str} :optional? false}]
       :open {:key {:kind :primitive :name :xt/str}
              :value {:kind :primitive :name :xt/bool}}})

^{:refer std.lang.typed.xtalk-infer/infer-vector :added "4.1"}
(fact "infers tuple types from vectors"
  (types/type->data (:type (infer-vector '["u1" 1] +ctx+)))
  => '{:kind :tuple
       :types [{:kind :primitive :name :xt/str}
               {:kind :primitive :name :xt/int}]})

^{:refer std.lang.typed.xtalk-infer/map-binding-updates :added "4.1"}
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

^{:refer std.lang.typed.xtalk-infer/binding-updates :added "4.1"}
(fact "builds env updates for binding targets"
  [(binding-updates '[a b] {:kind :tuple :types [types/+str-type+ types/+int-type+]} +ctx+)
   (binding-updates '#{id} +user-record+ +ctx+)]
  => '[{a {:kind :primitive :name :xt/str}
         b {:kind :primitive :name :xt/int}}
        {id {:kind :primitive :name :xt/str}}])

^{:refer std.lang.typed.xtalk-infer/infer-binding-form :added "4.1"}
(fact "infers binding forms and updates env"
  (infer-binding-form '(:= [a b] ["u1" 1]) +ctx+)
  => '{:type {:kind :tuple
              :types [{:kind :primitive :name :xt/str}
                      {:kind :primitive :name :xt/int}]}
       :errors []
       :env {a {:kind :primitive :name :xt/str}
             b {:kind :primitive :name :xt/int}}})

^{:refer std.lang.typed.xtalk-infer/infer-let :added "4.1"}
(fact "infers let bodies with bound symbols"
  (types/type->data (:type (infer-let '(let [(:xt/int count) 1]
                                         count)
                                      +ctx+)))
  => '{:kind :primitive :name :xt/int})

^{:refer std.lang.typed.xtalk-infer/infer-if :added "4.1"}
(fact "unions then and else branches"
  (types/type->data (:type (infer-if '(if true "a" 1) +ctx+)))
  => '{:kind :union
       :types [{:kind :primitive :name :xt/str}
               {:kind :primitive :name :xt/int}]})

^{:refer std.lang.typed.xtalk-infer/infer-ternary :added "4.1"}
(fact "treats ternary as if"
  (types/type->data (:type (infer-ternary '(:? true "a" 1) +ctx+)))
  => '{:kind :union
       :types [{:kind :primitive :name :xt/str}
               {:kind :primitive :name :xt/int}]})

^{:refer std.lang.typed.xtalk-infer/infer-or :added "4.1"}
(fact "strips nil-like members from or results"
  (types/type->data (:type (infer-or '(or maybe-id "guest") {:env '{maybe-id {:kind :maybe :item {:kind :primitive :name :xt/str}}} :ns 'sample.route :aliases {}})))
  => '{:kind :primitive :name :xt/str})

^{:refer std.lang.typed.xtalk-infer/infer-when :added "4.1"}
(fact "returns nil-or-body for when"
  (types/type->data (:type (infer-when '(when true 1) +ctx+)))
  => '{:kind :union
       :types [{:kind :primitive :name :xt/nil}
               {:kind :primitive :name :xt/int}]})

^{:refer std.lang.typed.xtalk-infer/infer-cond :added "4.1"}
(fact "unions cond branch outputs"
  (types/type->data (:type (infer-cond '(cond flag "a" :else 1) {:env '{flag {:kind :primitive :name :xt/bool}} :ns 'sample.route :aliases {}})))
  => '{:kind :union
       :types [{:kind :primitive :name :xt/str}
               {:kind :primitive :name :xt/int}]})

^{:refer std.lang.typed.xtalk-infer/infer-while :added "4.1"}
(fact "models while as nil-returning"
  (types/type->data (:type (infer-while '(while true 1) +ctx+)))
  => '{:kind :primitive :name :xt/nil})

^{:refer std.lang.typed.xtalk-infer/infer-yield :added "4.1"}
(fact "models yield as nil-returning"
  (types/type->data (:type (infer-yield '(yield "a") +ctx+)))
  => '{:kind :primitive :name :xt/nil})

^{:refer std.lang.typed.xtalk-infer/infer-anon-fn :added "4.1"}
(fact "infers anonymous fn shapes"
  (types/type->data (:type (infer-anon-fn '(fn [x] x) +ctx+)))
  => '{:kind :fn
       :inputs [{:kind :primitive :name :xt/unknown}]
       :output {:kind :primitive :name :xt/unknown}})

^{:refer std.lang.typed.xtalk-infer/merge-record-fields :added "4.1"}
(fact "merges record fields by name"
  (vec (merge-record-fields [{:name "id" :type types/+str-type+ :optional? false}]
                            [{:name "count" :type types/+int-type+ :optional? false}]))
  => '[{:name "id" :type {:kind :primitive :name :xt/str} :optional? false}
        {:name "count" :type {:kind :primitive :name :xt/int} :optional? false}])

^{:refer std.lang.typed.xtalk-infer/merge-open-types :added "4.1"}
(fact "merges open dict-like types"
  (merge-open-types {:key types/+str-type+ :value types/+bool-type+}
                    {:key types/+str-type+ :value types/+int-type+})
  => '{:key {:kind :primitive :name :xt/str}
       :value {:kind :union
               :types [{:kind :primitive :name :xt/bool}
                       {:kind :primitive :name :xt/int}]}})

^{:refer std.lang.typed.xtalk-infer/infer-obj-assign :added "4.1"}
(fact "merges record and dict object assignments"
  (types/type->data (:type (infer-obj-assign '(x:obj-assign {:id "u1"} {:count 1}) +ctx+)))
  => '{:kind :record
       :fields [{:name "id" :type {:kind :primitive :name :xt/str} :optional? false}
                {:name "count" :type {:kind :primitive :name :xt/int} :optional? false}]})

^{:refer std.lang.typed.xtalk-infer/arrayify-type :added "4.1"}
(fact "converts items and unions to arrays"
  [(types/type->data (arrayify-type types/+str-type+ +ctx+))
   (types/type->data (arrayify-type {:kind :union :types [{:kind :array :item types/+str-type+} types/+int-type+]} +ctx+))]
  => '[{:kind :array :item {:kind :primitive :name :xt/str}}
        {:kind :array :item {:kind :union :types [{:kind :primitive :name :xt/str}
                                                  {:kind :primitive :name :xt/int}]}}])

^{:refer std.lang.typed.xtalk-infer/infer-make-container :added "4.1"}
(fact "infers make-container records"
  (types/type->data (:type (infer-make-container '(xt.lang.event-common/make-container initializer "route" {:tag true})
                                                 {:env '{initializer {:kind :fn :inputs [] :output {:kind :primitive :name :xt/int}}}
                                                  :ns 'sample.route :aliases {}})))
  => '{:kind :record
       :fields [{:name "::" :type {:kind :primitive :name :xt/str} :optional? false}
                {:name "listeners" :type {:kind :named :name xt.lang.event-common/EventListenerMap} :optional? false}
                {:name "data" :type {:kind :primitive :name :xt/int} :optional? false}
                {:name "initial" :type {:kind :fn :inputs [] :output {:kind :primitive :name :xt/int}} :optional? false}
                {:name "tag" :type {:kind :primitive :name :xt/bool} :optional? false}]})

^{:refer std.lang.typed.xtalk-infer/infer-blank-container :added "4.1"}
(fact "infers blank-container records"
  (types/type->data (:type (infer-blank-container '(xt.lang.event-common/blank-container "route" {:tag true}) +ctx+)))
  => '{:kind :record
       :fields [{:name "::" :type {:kind :primitive :name :xt/str} :optional? false}
                {:name "listeners" :type {:kind :named :name xt.lang.event-common/EventListenerMap} :optional? false}
                {:name "tag" :type {:kind :primitive :name :xt/bool} :optional? false}]})

^{:refer std.lang.typed.xtalk-infer/apply-default-type :added "4.1"}
(fact "adds default values to maybe lookups"
  (types/type->data (apply-default-type {:kind :maybe :item types/+str-type+} types/+int-type+))
  => '{:kind :union
       :types [{:kind :primitive :name :xt/str}
               {:kind :primitive :name :xt/int}]})

^{:refer std.lang.typed.xtalk-infer/infer-fixed-output :added "4.1"}
(fact "builds fixed-output builtin inferencers"
  (let [f (infer-fixed-output types/+bool-type+)]
    (f '(x:nil? :bad) +ctx+))
  => '{:type {:kind :primitive :name :xt/bool}
       :errors []})

^{:refer std.lang.typed.xtalk-infer/object-value-type :added "4.1"}
(fact "collects possible object value types"
  [(types/type->data (object-value-type +user-record+ +ctx+))
   (types/type->data (object-value-type +dict-users+ +ctx+))]
  => '[{:kind :union
         :types [{:kind :primitive :name :xt/str}
                 {:kind :primitive :name :xt/int}]}
        {:kind :record
         :fields [{:name "id" :type {:kind :primitive :name :xt/str} :optional? false}
                  {:name "count" :type {:kind :primitive :name :xt/int} :optional? false}]}])

^{:refer std.lang.typed.xtalk-infer/infer-obj-vals :added "4.1"}
(fact "infers obj-vals arrays"
  (types/type->data (:type (infer-obj-vals '(x:obj-vals user) {:env '{user {:kind :record :fields [{:name "id" :type {:kind :primitive :name :xt/str} :optional? false}
                                                                                                     {:name "count" :type {:kind :primitive :name :xt/int} :optional? false}]}}
                                                         :ns 'sample.route :aliases {}})))
  => '{:kind :array
       :item {:kind :union
              :types [{:kind :primitive :name :xt/str}
                      {:kind :primitive :name :xt/int}]}})

^{:refer std.lang.typed.xtalk-infer/infer-obj-pairs :added "4.1"}
(fact "infers obj-pairs arrays"
  (types/type->data (:type (infer-obj-pairs '(x:obj-pairs user) {:env '{user {:kind :record :fields [{:name "id" :type {:kind :primitive :name :xt/str} :optional? false}
                                                                                                       {:name "count" :type {:kind :primitive :name :xt/int} :optional? false}]}}
                                                           :ns 'sample.route :aliases {}})))
  => '{:kind :array
       :item {:kind :tuple
              :types [{:kind :primitive :name :xt/str}
                      {:kind :union
                       :types [{:kind :primitive :name :xt/str}
                               {:kind :primitive :name :xt/int}]}]}})

^{:refer std.lang.typed.xtalk-infer/infer-obj-clone :added "4.1"}
(fact "clones record-shaped objects"
  (types/type->data (:type (infer-obj-clone '(x:obj-clone user) {:env '{user {:kind :record :fields [{:name "id" :type {:kind :primitive :name :xt/str} :optional? false}]}}
                                                           :ns 'sample.route :aliases {}})))
  => '{:kind :record
       :fields [{:name "id" :type {:kind :primitive :name :xt/str} :optional? false}]})

^{:refer std.lang.typed.xtalk-infer/infer-arr-clone :added "4.1"}
(fact "clones tuple arrays into array unions"
  (types/type->data (:type (infer-arr-clone '(x:arr-clone values) {:env '{values {:kind :tuple :types [{:kind :primitive :name :xt/str}
                                                                                                          {:kind :primitive :name :xt/int}]}}
                                                           :ns 'sample.route :aliases {}})))
  => '{:kind :array
       :item {:kind :union
              :types [{:kind :primitive :name :xt/str}
                      {:kind :primitive :name :xt/int}]}})

^{:refer std.lang.typed.xtalk-infer/intrinsic-callbacks :added "4.1"}
(fact "exposes callback hooks for intrinsic dispatch"
  (set (keys (intrinsic-callbacks)))
  => '#{:result :infer-type :resolve-type :arrayify-type :infer-get-key :infer-get-path :infer-obj-assign :infer-make-container :infer-blank-container})

^{:refer std.lang.typed.xtalk-infer/wildcard-callable? :added "4.1"}
(fact "recognizes wildcard-callable types"
  [(wildcard-callable? types/+unknown-type+ +ctx+)
   (wildcard-callable? {:kind :maybe :item {:kind :primitive :name :xt/any}} +ctx+)
   (wildcard-callable? {:kind :primitive :name :xt/str} +ctx+)]
  => [true true false])

^{:refer std.lang.typed.xtalk-infer/callable-types :added "4.1"}
(fact "extracts callable members from unions"
  (mapv types/type->data
        (callable-types {:kind :union
                         :types [{:kind :fn :inputs [types/+str-type+] :output types/+bool-type+}
                                 {:kind :primitive :name :xt/int}]}
                        +ctx+))
  => '[{:kind :fn
         :inputs [{:kind :primitive :name :xt/str}]
         :output {:kind :primitive :name :xt/bool}}])

^{:refer std.lang.typed.xtalk-infer/call-arg-errors :added "4.1"}
(fact "reports call arg mismatches"
  (call-arg-errors [(result types/+int-type+) (result types/+str-type+)]
                   [types/+str-type+ types/+str-type+]
                   '[1 "x"]
                   +ctx+)
  => '[{:tag :call-arg-type-mismatch
         :form 1
         :expected {:kind :primitive :name :xt/str}
         :actual {:kind :primitive :name :xt/int}}])

^{:refer std.lang.typed.xtalk-infer/optional-arity? :added "4.1"}
(fact "accepts missing optional trailing args"
  [(optional-arity? [types/+str-type+ {:kind :maybe :item types/+int-type+}] 1 +ctx+)
   (optional-arity? [types/+str-type+ types/+int-type+] 1 +ctx+)]
  => [true false])

^{:refer std.lang.typed.xtalk-infer/infer-function-call :added "4.1"}
(fact "infers callable outputs and call errors"
  [(types/type->data (:type (infer-function-call '(handler "u1") {:env '{handler {:kind :fn :inputs [{:kind :primitive :name :xt/str}] :output {:kind :primitive :name :xt/bool}}}
                                                           :ns 'sample.route :aliases {}})))
   (-> (infer-function-call '(value "u1") {:env '{value {:kind :primitive :name :xt/int}} :ns 'sample.route :aliases {}}) :errors first :tag)]
  => '[{:kind :primitive :name :xt/bool}
        :not-callable])

^{:refer std.lang.typed.xtalk-infer/infer-get-key :added "4.1"}
(fact "infers keyed access with defaults"
  (types/type->data (:type (infer-get-key '(x:get-key users "u1" "guest") {:env '{users {:kind :dict :key {:kind :primitive :name :xt/str} :value {:kind :primitive :name :xt/str}}}
                                                     :ns 'sample.route :aliases {}})))
  => '{:kind :primitive :name :xt/str})

^{:refer std.lang.typed.xtalk-infer/infer-get-idx :added "4.1"}
(fact "infers indexed access with defaults"
  (types/type->data (:type (infer-get-idx '(x:get-idx values 0 "guest") {:env '{values {:kind :array :item {:kind :primitive :name :xt/str}}}
                                                     :ns 'sample.route :aliases {}})))
  => '{:kind :primitive :name :xt/str})

^{:refer std.lang.typed.xtalk-infer/infer-get-path :added "4.1"}
(fact "infers path access through nested records"
  (types/type->data (:type (infer-get-path '(x:get-path route ["user" "id"] nil)
                                           {:env '{route {:kind :record
                                                          :fields [{:name "user"
                                                                    :type {:kind :record
                                                                           :fields [{:name "id" :type {:kind :primitive :name :xt/str} :optional? false}]}
                                                                    :optional? false}]}}
                                            :ns 'sample.route :aliases {}})))
  => '{:kind :primitive :name :xt/str})

^{:refer std.lang.typed.xtalk-infer/infer-builtin-form :added "4.1"}
(fact "dispatches builtin inference rules"
  [(types/type->data (:type (infer-builtin-form (ops/canonical-entry 'x:get-key)
                                                '(x:get-key route "id")
                                                {:env '{route {:kind :record :fields [{:name "id" :type {:kind :primitive :name :xt/str} :optional? false}]}}
                                                 :ns 'sample.route :aliases {}})))
   (types/type->data (:type (infer-builtin-form (ops/canonical-entry 'x:add)
                                                '(x:add 1 2)
                                                +ctx+)))
   (:tag (first (:errors (infer-builtin-form (ops/canonical-entry 'x:add)
                                             '(x:add 1)
                                             +ctx+))))]
  => '[{:kind :primitive :name :xt/str}
        {:kind :primitive :name :xt/num}
        :call-arity-mismatch])

^{:refer std.lang.typed.xtalk-infer/infer-dot :added "4.1"}
(fact "dispatches dot access by key or path"
  [(types/type->data (:type (infer-dot '(. route "id") {:env '{route {:kind :record :fields [{:name "id" :type {:kind :primitive :name :xt/str} :optional? false}]}}
                                                   :ns 'sample.route :aliases {}})))
   (types/type->data (:type (infer-dot '(. route ["id"]) {:env '{route {:kind :record :fields [{:name "id" :type {:kind :primitive :name :xt/str} :optional? false}]}}
                                                     :ns 'sample.route :aliases {}})))]
  => '[{:kind :primitive :name :xt/str}
        {:kind :primitive :name :xt/str}])

^{:refer std.lang.typed.xtalk-infer/infer-free :added "4.1"}
(fact "treats free forms as wrapped literals"
  [(types/type->data (:type (infer-free '(:- "ok") +ctx+)))
   (types/type->data (:type (infer-free '(:-) +ctx+)))]
  => '[{:kind :primitive :name :xt/str}
        {:kind :primitive :name :xt/unknown}])

^{:refer std.lang.typed.xtalk-infer/infer-keyword-call :added "4.1"}
(fact "treats keyword calls as keyed access"
  (types/type->data (:type (infer-keyword-call '(:entry opts) {:env '{opts {:kind :record :fields [{:name "entry" :type {:kind :primitive :name :xt/str} :optional? false}]}}
                                                         :ns 'sample.route :aliases {}})))
  => '{:kind :primitive :name :xt/str})

^{:refer std.lang.typed.xtalk-infer/infer-body :added "4.1"}
(fact "returns last body type and threaded env"
  (infer-body '[(:= [a b] ["u1" 1]) a] +ctx+)
  => '{:type {:kind :primitive :name :xt/str}
       :errors []
       :env {a {:kind :primitive :name :xt/str}
             b {:kind :primitive :name :xt/int}}})

^{:refer std.lang.typed.xtalk-infer/infer-type :added "4.1"}
(fact "dispatches across literals lowered forms and calls"
  [(types/type->data (:type (infer-type '(k/get-key route "id") {:env '{route {:kind :record :fields [{:name "id" :type {:kind :primitive :name :xt/str} :optional? false}]}}
                                                           :ns 'sample.route :aliases '{k xt.lang.base-lib}})))
   (types/type->data (:type (infer-type '(if true 1 2) +ctx+)))]
  => '[{:kind :primitive :name :xt/str}
        {:kind :primitive :name :xt/int}])


^{:refer std.lang.typed.xtalk-infer/infer-op-spec-form :added "4.1"}
(fact "infers result type from builtin op-spec for a given call form"
  (types/type->data
   (:type (infer-op-spec-form (ops/canonical-entry 'x:add)
                              '(x:add 1 2)
                              +ctx+)))
  => {:kind :primitive :name :xt/num})

(fact "infers xt self outputs from the first argument"
  (-> (infer-op-spec-form (ops/canonical-entry 'x:arr-push)
                          '(x:arr-push items "a")
                          {:env '{items {:kind :array
                                         :item {:kind :primitive :name :xt/str}}}
                           :ns 'sample.route
                           :aliases {}})
      :type
      types/type->data)
  => '{:kind :array
       :item {:kind :primitive :name :xt/str}})
