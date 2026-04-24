(ns std.lang.model.spec-xtalk-typed-test
  (:require [clojure.string]
   [std.lang.typed.xtalk :as typed]
   [std.lang.typed.xtalk-analysis :as analysis]
   [std.lang.typed.xtalk-common :as types]
   [std.lang.typed.xtalk-infer :as infer]
   [std.lang.typed.xtalk-lower :as lower]
   [std.lang.typed.xtalk-ops :as ops]
   [std.lang.typed.xtalk-parse :as parse]
   [std.lang.model.spec-js.ts :as ts])
  (:use code.test))

(fact "normalizes xtalk type forms"
  (types/type->data
   (types/normalize-type
    '[:fn [UserMap :xt/str] [:xt/maybe User]]
    {:ns 'sample.user
     :aliases {}}))
  => '{:kind :fn
       :inputs [{:kind :named :name sample.user/UserMap}
                {:kind :primitive :name :xt/str}]
       :output {:kind :maybe
                :item {:kind :named :name sample.user/User}}})

(fact "defspec.xt registers a spec declaration"
  (typed/clear-registry!)
  (eval '(std.lang.typed.xtalk/defspec.xt LocalId :xt/str))
  (-> (typed/get-spec 'std.lang.model.spec-xtalk-typed-test/LocalId)
      :type
      types/type->data)
  => {:kind :primitive :name :xt/str})

(fact "registry entries expose explicit declaration kinds"
  (typed/clear-registry!)
  (typed/analyze-and-register! 'xt.lang.spec-base)
  (typed/analyze-and-register! 'xt.db.base-scope)
  [(-> (typed/get-entry 'xt.lang.spec-base/x:add)
       types/entry-kinds
       set)
   (types/declaration-kind (typed/get-macro 'xt.lang.spec-base/x:add))
   (types/declaration-kind (typed/get-value 'xt.db.base-scope/Scopes))
   (nil? (typed/get-type 'xt.lang.spec-base/x:add))
   (= :value (-> (typed/get-entry 'xt.db.base-scope/Scopes)
                  types/entry-primary-kind))]
  => '[#{:macro :spec}
        :macro
        :value
        false
        true])

(fact "defspec.xt resolves aliased type names during registration"
  (typed/clear-registry!)
  (eval '(std.lang.typed.xtalk/defspec.xt AliasMaybeFixture
           [:xt/maybe types/User]))
  (-> (typed/get-spec 'std.lang.model.spec-xtalk-typed-test/AliasMaybeFixture)
      :type
      types/type->data)
  => '{:kind :maybe
       :item {:kind :named
              :name std.lang.typed.xtalk-common/User}})

(fact "normalizes record field names to snake_case strings"
  [(types/field-key :hello-world)
   (types/field-key 'event-meta)
   (types/field-key "route-path")]
  => ["hello_world" "event_meta" "route_path"])

(fact "lowers xtalk surface helpers to typed core"
  (lower/lower-form '(xt/x:get-key route "tree")
                    {:ns 'sample.route
                     :aliases '{k xt.lang.common-lib}})
  => '(x:get-key route "tree" nil))

(fact "canonicalizes builtin wrappers through grammar op tables"
  [(ops/canonical-symbol 'xt.lang.common-data/obj-assign)
   (ops/canonical-symbol 'xt.lang.common-data/obj-vals)
   (ops/canonical-symbol 'x:str-len)
   (lower/lower-form '(k/obj-assign current extra)
                     {:ns 'sample.route
                      :aliases '{k xt.lang.common-data}})
   (lower/lower-form '(k/obj-vals current)
                     {:ns 'sample.route
                      :aliases '{k xt.lang.common-data}})
   (lower/lower-form '(k/obj-clone current)
                     {:ns 'sample.route
                      :aliases '{k xt.lang.common-data}})
   (lower/lower-form '(k/arr-clone path)
                     {:ns 'sample.route
                      :aliases '{k xt.lang.common-lib}})
   (lower/lower-form '(x:str-len path)
                     {:ns 'sample.route
                      :aliases '{k xt.lang.common-lib}})
   (lower/lower-form '(k/arr-join path "/")
                     {:ns 'sample.route
                      :aliases '{k xt.lang.common-lib}})]
  => '[x:obj-assign
        x:obj-vals
        x:str-len
          (x:obj-assign current extra)
          (x:obj-vals current)
          (x:obj-clone current)
          (x:arr-clone path)
          (x:str-len path)
          (x:str-join "/" path)])

(fact "lowering uses rule-driven wrapper rewrites"
  [(lower/lower-form '(xt/x:get-key route "tree")
                     {:ns 'sample.route
                      :aliases '{k xt.lang.common-lib}})
   (lower/lower-form '(k/get-in route ["tree" "leaf"] "fallback")
                     {:ns 'sample.route
                      :aliases '{k xt.lang.common-lib}})
   (lower/lower-form '(xtd/first items)
                     {:ns 'sample.route
                      :aliases '{k xt.lang.common-lib}})
   (lower/lower-form '(xtd/second items)
                     {:ns 'sample.route
                      :aliases '{k xt.lang.common-lib}})
   (lower/lower-form '(k/arrayify items)
                     {:ns 'sample.route
                      :aliases '{k xt.lang.common-lib}})]
  => '[(x:get-key route "tree" nil)
       (x:get-path route ["tree" "leaf"] "fallback")
       (x:get-idx items (x:offset))
       (x:get-idx items (x:offset 1))
       (std.lang.typed.xtalk-intrinsic/arrayify items)])

(fact "infers open object types for dynamic map keys"
  (-> (infer/infer-type '{:type "route.path" pkey true}
                        {:env '{pkey {:kind :primitive :name :xt/str}}
                         :ns 'sample.route
                         :aliases {}})
      :type
      types/type->data)
  => '{:kind :record
       :fields [{:name "type"
                  :type {:kind :primitive :name :xt/str}
                  :optional? false}]
        :open {:key {:kind :primitive :name :xt/str}
               :value {:kind :primitive :name :xt/bool}}})

(fact "infers canonical object helpers and defaulted access"
  [(-> (infer/infer-type '(k/obj-vals user)
                         {:env '{user {:kind :record
                                       :fields [{:name "name"
                                                 :type {:kind :primitive :name :xt/str}
                                                 :optional? false}
                                                {:name "count"
                                                 :type {:kind :primitive :name :xt/int}
                                                 :optional? false}]}}
                          :ns 'sample.route
                          :aliases '{k xt.lang.common-data}})
       :type
       types/type->data)
   (-> (infer/infer-type '(k/obj-pairs user)
                         {:env '{user {:kind :record
                                       :fields [{:name "name"
                                                 :type {:kind :primitive :name :xt/str}
                                                 :optional? false}
                                                {:name "count"
                                                 :type {:kind :primitive :name :xt/int}
                                                 :optional? false}]}}
                          :ns 'sample.route
                          :aliases '{k xt.lang.common-data}})
       :type
       types/type->data)
   (-> (infer/infer-type '(x:get-idx users 0 "guest")
                         {:env '{users {:kind :array
                                        :item {:kind :primitive :name :xt/str}}}
                          :ns 'sample.route
                          :aliases {}})
       :type
       types/type->data)
   (-> (infer/infer-type '(x:to-string count)
                         {:env '{count {:kind :primitive :name :xt/int}}
                          :ns 'sample.route
                          :aliases {}})
       :type
       types/type->data)]
  => '[{:kind :array
        :item {:kind :union
               :types [{:kind :primitive :name :xt/str}
                       {:kind :primitive :name :xt/int}]}}
       {:kind :array
        :item {:kind :tuple
               :types [{:kind :primitive :name :xt/str}
                       {:kind :union
                        :types [{:kind :primitive :name :xt/str}
                                {:kind :primitive :name :xt/int}]}]}}
       {:kind :primitive :name :xt/str}
       {:kind :primitive :name :xt/str}])

(fact "treats callable unions and unknown fn args permissively"
  [(infer/compatible-type?
    {:kind :fn
     :inputs [types/+unknown-type+]
     :output types/+str-type+}
    {:kind :fn
     :inputs [{:kind :named :name 'sample.route/ViewContext}]
     :output types/+str-type+}
    {:ns 'sample.route
     :aliases {}})
   (-> (infer/infer-type '(handler payload)
                         {:env '{handler {:kind :maybe
                                          :item {:kind :fn
                                                 :inputs [{:kind :primitive :name :xt/any}]
                                                 :output {:kind :primitive :name :xt/bool}}}
                                 payload {:kind :record
                                          :fields [{:name "type"
                                                    :type {:kind :primitive :name :xt/str}
                                                    :optional? false}]}}
                          :ns 'sample.route
                          :aliases {}})
       :type
       types/type->data)]
  => '[true
       {:kind :primitive :name :xt/bool}])

(fact "treats unknown and nil unions compatibly"
  [(infer/compatible-type?
    types/+unknown-type+
    {:kind :named :name 'sample.route/RoutePath}
    {:ns 'sample.route
     :aliases {}})
   (infer/compatible-type?
    {:kind :union
     :types [types/+nil-type+
             {:kind :array
              :item types/+str-type+}]}
    {:kind :maybe
     :item {:kind :array
            :item types/+str-type+}}
    {:ns 'sample.route
     :aliases {}})]
  => '[true true])

(fact "strips nil from or types and allows dynamic assignment targets"
  [(-> (infer/infer-type '(or maybe-path [])
                         {:env '{maybe-path {:kind :maybe
                                             :item {:kind :array
                                                    :item {:kind :primitive :name :xt/str}}}}
                          :ns 'sample.route
                          :aliases {}})
       :type
       types/type->data)
   (-> (infer/infer-type '(:= (x:get-key acc tag) [true err true])
                         {:env '{acc {:kind :dict
                                      :key {:kind :primitive :name :xt/str}
                                      :value {:kind :primitive :name :xt/any}}
                                 tag {:kind :primitive :name :xt/str}
                                 err {:kind :primitive :name :xt/str}}
                          :ns 'sample.route
                          :aliases {}})
       :errors)]
  => '[{:kind :union
        :types [{:kind :array
                 :item {:kind :primitive :name :xt/str}}
                {:kind :tuple
                 :types []}]}
       []])

(fact "supports maybe-callables, tuples, and optional trailing args"
  [(infer/compatible-type?
    {:kind :maybe
     :item {:kind :fn
            :inputs [{:kind :named :name 'sample.route/ViewEvent}]
            :output types/+bool-type+}}
    {:kind :maybe
     :item {:kind :fn
            :inputs [types/+str-type+]
            :output types/+bool-type+}}
    {:ns 'sample.route
     :aliases {}})
   (-> (infer/infer-type '[route disabled]
                         {:env '{route {:kind :named :name sample.route/Route}
                                 disabled {:kind :primitive :name :xt/bool}}
                          :ns 'sample.route
                          :aliases {}})
       :type
       types/type->data)
   (-> (infer/infer-type '(pipeline-call context "main" false async-fn hook-fn)
                         {:env '{pipeline-call {:kind :fn
                                                :inputs [{:kind :named :name sample.route/ViewContext}
                                                         {:kind :primitive :name :xt/str}
                                                         {:kind :primitive :name :xt/bool}
                                                         {:kind :primitive :name :xt/any}
                                                         {:kind :primitive :name :xt/any}
                                                         {:kind :maybe
                                                          :item {:kind :dict
                                                                 :key {:kind :primitive :name :xt/str}
                                                                 :value {:kind :primitive :name :xt/any}}}]
                                                :output {:kind :primitive :name :xt/any}}
                                 context {:kind :named :name sample.route/ViewContext}
                                 async-fn {:kind :primitive :name :xt/any}
                                 hook-fn {:kind :primitive :name :xt/any}}
                          :ns 'sample.route
                          :aliases {}})
        :errors)]
  => '[false
       {:kind :tuple
        :types [{:kind :named :name sample.route/Route}
                {:kind :primitive :name :xt/bool}]}
       []])

(fact "models cond while and yield explicitly"
  [(-> (infer/infer-type '(cond flag "a" :else 1)
                         {:env '{flag {:kind :primitive :name :xt/bool}}
                          :ns 'sample.route
                          :aliases {}})
       :type
       types/type->data)
   (-> (infer/infer-type '(while (< i finish)
                           (yield i)
                           (:= i (+ i step)))
                         {:env '{i {:kind :primitive :name :xt/int}
                                 finish {:kind :primitive :name :xt/int}
                                 step {:kind :primitive :name :xt/int}}
                          :ns 'sample.route
                          :aliases {}})
       :type
       types/type->data)
   (-> (infer/infer-type '(yield value)
                         {:env '{value {:kind :primitive :name :xt/str}}
                          :ns 'sample.route
                          :aliases {}})
       :type
       types/type->data)]
  => '[{:kind :union
        :types [{:kind :primitive :name :xt/str}
                {:kind :primitive :name :xt/int}]}
       {:kind :primitive :name :xt/nil}
       {:kind :primitive :name :xt/nil}])

(fact "infers iterator predicate builtins as booleans"
  [(-> (infer/infer-type '(x:iter-native? it)
                         {:env '{it {:kind :primitive :name :xt/unknown}}
                          :ns 'sample.route
                          :aliases {}})
       :type
       types/type->data)
   (-> (infer/infer-type '(x:iter-has? value)
                         {:env '{value {:kind :primitive :name :xt/unknown}}
                          :ns 'sample.route
                          :aliases {}})
       :type
       types/type->data)
   (-> (infer/infer-type '(x:iter-eq left right eq-fn)
                         {:env '{left {:kind :primitive :name :xt/unknown}
                                 right {:kind :primitive :name :xt/unknown}
                                 eq-fn {:kind :primitive :name :xt/unknown}}
                          :ns 'sample.route
                          :aliases {}})
       :type
       types/type->data)]
  => '[{:kind :primitive :name :xt/bool}
       {:kind :primitive :name :xt/bool}
       {:kind :primitive :name :xt/bool}])

(fact "infers fn:> as a zero-arg constant function"
  (-> (infer/infer-type '(std.lang.typed.xtalk-intrinsic/const-fn "ok")
                        {:ns 'sample.route
                         :aliases {}})
      :type
      types/type->data)
  => '{:kind :fn
       :inputs []
       :output {:kind :primitive :name :xt/str}})

(fact "zero-arg callbacks satisfy wider callback slots"
  (infer/compatible-type?
   {:kind :fn
    :inputs []
    :output {:kind :primitive :name :xt/bool}}
   {:kind :maybe
    :item {:kind :fn
           :inputs [{:kind :primitive :name :xt/any}]
           :output {:kind :primitive :name :xt/bool}}}
   {:ns 'sample.route
    :aliases {}})
  => true)

(fact "parses defspec.xt and merges function signatures"
  (let [analysis (parse/analyze-namespace 'std.lang.model.spec-xtalk-typed-fixture)
        fn-def (some #(when (= "find-user" (:name %)) %)
                     (:functions analysis))]
    {:spec-count (count (:specs analysis))
     :input-types (mapv (comp types/type->data :type) (:inputs fn-def))
     :output (types/type->data (:output fn-def))})
  => '{:spec-count 3
       :input-types [{:kind :named :name std.lang.model.spec-xtalk-typed-fixture/UserMap}
                     {:kind :primitive :name :xt/str}]
        :output {:kind :maybe
                 :item {:kind :named :name std.lang.model.spec-xtalk-typed-fixture/User}}})

(fact "parses single-clause multi-arity forms tolerantly"
  (let [fn-def (parse/parse-defn
                '(defn.xt prototype-create
                   ([m]
                    (x:prototype-create m)))
                'xt.lang.common-lib
                {})]
    {:inputs (mapv (comp types/type->data :type) (:inputs fn-def))
     :body (:raw-body fn-def)})
  => '{:inputs [{:kind :primitive :name :xt/unknown}]
     :body [(x:prototype-create m)]})

(fact "parses rest and destructured args tolerantly"
  (let [fn-def (parse/parse-defn
                '(defn.xt with-opts
                   [& [m]]
                   (return m))
                'xt.lang.common-lib
                {})]
    (mapv :name (:inputs fn-def)))
  => '[m])

(fact "marks defgen.xt declarations as generators"
  (let [fn-def (parse/parse-defn
                '(defgen.xt repeatedly
                   [f]
                   (while true
                     (yield (f))))
                'sample.route
                {})]
    (true? (types/generator-def? fn-def)))
  => true)

(fact "parses defmacro.xt declarations explicitly"
  (let [macro-def (parse/parse-defmacro
                   '(defmacro.xt ^{:standalone true}
                      add
                      "performs add operation"
                      {:added "4.0"}
                      [a b]
                      (list '+ a b))
                   'sample.route
                   {})]
    [(:name macro-def)
     (mapv :name (:inputs macro-def))
     (get-in macro-def [:body-meta :macro])
     (types/type->data (:output macro-def))])
  => '["add"
       [a b]
       true
       {:kind :primitive :name :xt/unknown}])

(fact "parses def.xt value declarations explicitly"
  (let [value-def (parse/parse-defvalue
                   '(def.xt ^{:- [:xt/dict :xt/str :xt/num]}
                      ScopeMap
                      {:a 1})
                   'sample.route
                   {})]
    [(:name value-def)
     (types/type->data (:type value-def))
     (get-in value-def [:body-meta :def])
     (:raw-value value-def)])
  => '["ScopeMap"
       {:kind :dict
        :key {:kind :primitive :name :xt/str}
        :value {:kind :primitive :name :xt/num}}
       true
       {:a 1}])

(fact "attaches same-name specs to parsed values"
  (let [spec (parse/parse-spec-decl 'sample.route 'ScopeMap
                                    '[:xt/dict :xt/str :xt/num]
                                    {}
                                    {})
        value-def (parse/parse-defvalue
                   '(def.xt ScopeMap {:a 1})
                   'sample.route
                   {})
        attached (parse/attach-value-spec value-def spec)]
    [(types/type->data (:type attached))
     (some? (:spec attached))])
  => '[{:kind :dict
         :key {:kind :primitive :name :xt/str}
         :value {:kind :primitive :name :xt/num}}
        true])

(fact "analyzes macro and value declarations separately"
  [(-> (parse/analyze-namespace 'xt.lang.spec-base)
       :macros
       count
       pos?)
   (-> (parse/analyze-namespace 'xt.db.base-scope)
       :values
       count
       pos?)]
  => '[true true])

(fact "registers macros and values in the typed registry"
  (typed/clear-registry!)
  (typed/analyze-and-register! 'xt.lang.spec-base)
  (typed/analyze-and-register! 'xt.db.base-scope)
  [(some? (typed/get-macro 'xt.lang.spec-base/x:add))
   (true? (get-in (typed/get-macro 'xt.lang.spec-base/x:add) [:body-meta :macro]))
   (some? (typed/get-value 'xt.db.base-scope/Scopes))
   (true? (get-in (typed/get-value 'xt.db.base-scope/Scopes) [:body-meta :def]))
   (some? (typed/get-declaration 'xt.db.base-scope/Scopes :value))
   (pos? (count (typed/list-entries)))]
  => '[true true true true true true])

(fact "emits TypeScript declarations from xtalk specs"
  (let [out (ts/emit-namespace-declarations 'std.lang.model.spec-xtalk-typed-fixture)]
    [(clojure.string/includes? out "export interface User")
     (clojure.string/includes? out "export type UserMap = Record<string, User>;")
     (clojure.string/includes? out "export type find_user = (arg0: UserMap, arg1: string) => User | null;")
     (clojure.string/includes? out "export type wrong_user_name")
     (clojure.string/includes? out "export type find_user_wrong_key")])
  => [true true true true true])

(fact "supports map destructuring in let bindings"
  (-> (infer/infer-type
       '(let [{:keys [id namespace]} entry]
          (return id))
       {:env '{entry {:kind :record
                      :fields [{:name "id"
                                :type {:kind :primitive :name :xt/str}
                                :optional? false}
                               {:name "namespace"
                                :type {:kind :primitive :name :xt/str}
                                :optional? false}]}}
        :ns 'sample.route
        :aliases {}})
      :type
      types/type->data)
  => '{:kind :primitive :name :xt/str})

(fact "treats free forms as wrapped literals"
  (-> (infer/infer-type
       '(:- "0x55555555")
       {:env {}
        :ns 'sample.route
        :aliases {}})
      :type
      types/type->data)
  => '{:kind :primitive :name :xt/str})

(fact "treats keyword calls as keyed access"
  (-> (infer/infer-type
       '(:entry opts)
       {:env '{opts {:kind :record
                     :fields [{:name "entry"
                               :type {:kind :primitive :name :xt/str}
                               :optional? false}]}}
        :ns 'sample.route
        :aliases {}})
      :type
      types/type->data)
  => '{:kind :primitive :name :xt/str})

(fact "checks valid xtalk functions"
  (typed/clear-registry!)
  (typed/analyze-and-register! 'std.lang.model.spec-xtalk-typed-fixture)
  (-> (typed/check-function 'std.lang.model.spec-xtalk-typed-fixture/find-user)
      (select-keys [:return :errors]))
  => '{:return {:kind :maybe
                :item {:kind :named :name std.lang.model.spec-xtalk-typed-fixture/User}}
       :errors []})

(fact "reports return and call mismatches"
  (typed/clear-registry!)
  (typed/analyze-and-register! 'std.lang.model.spec-xtalk-typed-fixture)
  [(-> (typed/check-function 'std.lang.model.spec-xtalk-typed-fixture/wrong-user-name)
       :errors
       first
       :tag)
   (-> (typed/check-function 'std.lang.model.spec-xtalk-typed-fixture/find-user-wrong-key)
       :errors
       first
       :tag)]
  => [:return-type-mismatch :call-arg-type-mismatch])

(fact "exposes typed analysis helpers"
  (typed/clear-registry!)
  (typed/analyze-and-register! 'std.lang.model.spec-xtalk-typed-fixture)
  [(analysis/get-function-input-type 'std.lang.model.spec-xtalk-typed-fixture/find-user 'id)
   (analysis/get-function-output-type 'std.lang.model.spec-xtalk-typed-fixture/find-user)]
  => '[{:kind :primitive :name :xt/str}
       {:kind :maybe
        :item {:kind :named :name std.lang.model.spec-xtalk-typed-fixture/User}}])

(fact "analyzes event-common and event-form namespace specs"
  (typed/clear-registry!)
  (typed/analyze-and-register! 'xt.lang.event-common)
  (typed/analyze-and-register! 'xt.lang.event-form)
  [(analysis/get-function-output-type 'xt.lang.event-common/blank-container)
   (analysis/get-function-output-type 'xt.lang.event-common/add-listener)
   (analysis/get-function-output-type 'xt.lang.event-form/make-form)
   (analysis/get-function-output-type 'xt.lang.event-form/get-result)]
  => '[{:kind :named :name xt.lang.event-common/EventContainer}
       {:kind :named :name xt.lang.event-common/EventListenerEntry}
       {:kind :named :name xt.lang.event-form/EventForm}
       {:kind :named :name xt.lang.event-form/ValidationResult}])

(fact "analyzes event-route, event-view, and event-box namespace specs"
  (typed/clear-registry!)
  (typed/analyze-and-register! 'xt.lang.event-route)
  (typed/analyze-and-register! 'xt.lang.event-view)
  (typed/analyze-and-register! 'xt.lang.event-box)
  [(analysis/get-function-output-type 'xt.lang.event-route/make-route)
   (analysis/get-function-output-type 'xt.lang.event-route/get-param)
   (analysis/get-function-output-type 'xt.lang.event-view/create-view)
   (analysis/get-function-output-type 'xt.lang.event-view/pipeline-prep)
   (analysis/get-function-output-type 'xt.lang.event-box/make-box)]
  => '[{:kind :named :name xt.lang.event-route/EventRoute}
       {:kind :maybe
        :item {:kind :primitive :name :xt/str}}
       {:kind :named :name xt.lang.event-view/EventView}
       {:kind :tuple
        :types [{:kind :named :name xt.lang.event-view/ViewContext}
                {:kind :primitive :name :xt/bool}]}
       {:kind :named :name xt.lang.event-box/EventBox}])

(fact "check-namespace can analyze event-route without crashing"
  (typed/clear-registry!)
  (-> (typed/check-namespace 'xt.lang.event-route)
      :namespace)
  => 'xt.lang.event-route)



(fact "analyzes js cell kernel specs"
  (typed/clear-registry!)
  (doseq [ns-sym '[js.cell.kernel.spec
                   js.cell.kernel.base-util
                   js.cell.kernel.base-link
                   js.cell.kernel.base-impl
                   js.cell.kernel.base-model
                   js.cell.kernel.worker-state
                   js.cell.kernel.worker-local
                   js.cell.kernel.worker-impl
                   js.cell.kernel.worker-mock
                   js.cell.kernel.base-link-local
                   js.cell.kernel.base-link-eval]]
    (typed/analyze-and-register! ns-sym))
  [(-> (typed/get-spec 'js.cell.kernel.base-util/req-frame)
       :type
       types/type->data)
   (-> (typed/get-spec 'js.cell.kernel.base-link/link-create)
       :type
       types/type->data)
   (-> (typed/get-spec 'js.cell.kernel.base-impl/new-cell)
       :type
       types/type->data)
   (-> (typed/get-spec 'js.cell.kernel.base-model/create-view)
       :type
       types/type->data)
   (-> (typed/get-spec 'js.cell.kernel.worker-state/fn-get-action-entry)
       :type
       types/type->data)
   (-> (typed/get-spec 'js.cell.kernel.base-link-eval/post-eval)
       :type
       types/type->data)]
  => '[{:kind :fn
         :inputs [{:kind :primitive :name :xt/str}
                  {:kind :maybe
                   :item {:kind :primitive :name :xt/str}}
                  {:kind :primitive :name :xt/any}
                  {:kind :maybe
                   :item {:kind :named :name js.cell.kernel.spec/AnyMap}}
                  {:kind :maybe
                   :item {:kind :named :name js.cell.kernel.spec/AnyMap}}]
         :output {:kind :named :name js.cell.kernel.spec/RequestFrame}}
        {:kind :fn
         :inputs [{:kind :primitive :name :xt/any}]
         :output {:kind :named :name js.cell.kernel.spec/LinkRecord}}
        {:kind :fn
         :inputs [{:kind :primitive :name :xt/any}]
         :output {:kind :named :name js.cell.kernel.spec/CellRecord}}
        {:kind :fn
         :inputs [{:kind :named :name js.cell.kernel.spec/CellRecord}
                  {:kind :primitive :name :xt/str}
                  {:kind :primitive :name :xt/str}
                  {:kind :named :name js.cell.kernel.spec/ViewSpec}]
         :output {:kind :named :name js.cell.kernel.spec/ViewRecord}}
        {:kind :fn
         :inputs [{:kind :primitive :name :xt/str}]
         :output {:kind :maybe
                  :item {:kind :named :name js.cell.kernel.spec/WorkerActionEntry}}}
        {:kind :fn
         :inputs [{:kind :named :name js.cell.kernel.spec/LinkRecord}
                  {:kind :primitive :name :xt/any}
                  {:kind :maybe
                   :item {:kind :primitive :name :xt/bool}}
                  {:kind :maybe
                   :item {:kind :primitive :name :xt/str}}]
         :output {:kind :primitive :name :xt/any}}])

(fact "analyzes base namespace specs"
  (typed/clear-registry!)
  (doseq [ns-sym '[xt.lang.common-lib
                   xt.lang.spec-base
                   xt.lang.common-runtime
                   xt.lang.common-iter
                   xt.lang.common-repl
                   xt.lang.common-string
                   xt.lang.common-interval]]
    (typed/analyze-and-register! ns-sym))
  [(analysis/get-function-output-type 'xt.lang.common-string/sym-pair)
   (analysis/get-function-output-type 'xt.lang.common-runtime/xt-create)
   (analysis/get-function-output-type 'xt.lang.common-runtime/xt-current)
   (analysis/get-function-output-type 'xt.lang.common-iter/iter)
   (analysis/get-function-output-type 'xt.lang.common-lib/return-encode)
   (analysis/get-function-output-type 'xt.lang.common-string/tag-string)
   (analysis/get-function-output-type 'xt.lang.common-interval/start-interval)
   (-> (typed/get-macro 'xt.lang.spec-base/x:add)
       :output
       types/type->data)]
  => '[{:kind :primitive :name :xt/unknown}
        {:kind :named :name xt.lang.common-runtime/XTState}
        {:kind :maybe
         :item {:kind :named :name xt.lang.common-runtime/XTState}}
        {:kind :primitive :name :xt/unknown}
        {:kind :primitive :name :xt/unknown}
        {:kind :primitive :name :xt/str}
        {:kind :primitive :name :xt/any}
        {:kind :primitive :name :xt/num}])
