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
   (lower/lower-form '(k/obj-assign current extra)
                     {:ns 'sample.route
                      :aliases '{k xt.lang.base-lib}})
   (lower/lower-form '(k/arr-join path "/")
                     {:ns 'sample.route
                      :aliases '{k xt.lang.base-lib}})]
  => '[x:obj-assign
       (x:obj-assign current extra)
       (x:str-join "/" path)])

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
