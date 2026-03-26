(ns std.lang.model.spec-xtalk-typed-test
  (:require [std.lang.typed.xtalk :as typed]
   [std.lang.typed.xtalk-analysis :as analysis]
   [std.lang.typed.xtalk-common :as types]
   [std.lang.typed.xtalk-infer :as infer]
   [std.lang.typed.xtalk-lower :as lower]
   [std.lang.typed.xtalk-ops :as ops]
   [std.lang.typed.xtalk-parse :as parse])
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
  (lower/lower-form '(k/get-key route "tree")
                    {:ns 'sample.route
                     :aliases '{k xt.lang.base-lib}})
  => '(x:get-key route "tree" nil))

(fact "canonicalizes builtin wrappers through grammar op tables"
  [(ops/canonical-symbol 'xt.lang.base-lib/obj-assign)
   (ops/canonical-symbol 'xt.lang.base-lib/obj-vals)
   (ops/canonical-symbol 'x:str-len)
   (lower/lower-form '(k/obj-assign current extra)
                     {:ns 'sample.route
                      :aliases '{k xt.lang.base-lib}})
   (lower/lower-form '(k/obj-vals current)
                     {:ns 'sample.route
                      :aliases '{k xt.lang.base-lib}})
   (lower/lower-form '(k/obj-clone current)
                     {:ns 'sample.route
                      :aliases '{k xt.lang.base-lib}})
   (lower/lower-form '(k/arr-clone path)
                     {:ns 'sample.route
                      :aliases '{k xt.lang.base-lib}})
   (lower/lower-form '(x:str-len path)
                     {:ns 'sample.route
                      :aliases '{k xt.lang.base-lib}})
   (lower/lower-form '(k/arr-join path "/")
                     {:ns 'sample.route
                      :aliases '{k xt.lang.base-lib}})]
  => '[x:obj-assign
       x:obj-vals
       x:len
         (x:obj-assign current extra)
         (x:obj-vals current)
         (x:obj-clone current)
         (x:arr-clone path)
         (x:len path)
         (x:str-join "/" path)])

(fact "lowering uses rule-driven wrapper rewrites"
  [(lower/lower-form '(k/get-key route "tree")
                     {:ns 'sample.route
                      :aliases '{k xt.lang.base-lib}})
   (lower/lower-form '(k/get-in route ["tree" "leaf"] "fallback")
                     {:ns 'sample.route
                      :aliases '{k xt.lang.base-lib}})
   (lower/lower-form '(k/first items)
                     {:ns 'sample.route
                      :aliases '{k xt.lang.base-lib}})
   (lower/lower-form '(k/second items)
                     {:ns 'sample.route
                      :aliases '{k xt.lang.base-lib}})
   (lower/lower-form '(k/arrayify items)
                     {:ns 'sample.route
                      :aliases '{k xt.lang.base-lib}})]
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
                          :aliases '{k xt.lang.base-lib}})
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
                          :aliases '{k xt.lang.base-lib}})
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
                '(defn.xt proto-create
                   ([m]
                    (x:proto-create m)))
                'xt.lang.base-lib
                {})]
    {:inputs (mapv (comp types/type->data :type) (:inputs fn-def))
     :body (:raw-body fn-def)})
  => '{:inputs [{:kind :primitive :name :xt/unknown}]
     :body [(x:proto-create m)]})

(fact "parses rest and destructured args tolerantly"
  (let [fn-def (parse/parse-defn
                '(defn.xt with-opts
                   [& [m]]
                   (return m))
                'xt.lang.base-lib
                {})]
    (mapv :name (:inputs fn-def)))
  => '[m])

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
