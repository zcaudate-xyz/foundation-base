(ns rt.postgres.grammar.typed-jsonb-test
  (:require [rt.postgres.grammar.typed-common :as types]
            [rt.postgres.grammar.typed-jsonb :as jsonb])
  (:use code.test))

^{:refer rt.postgres.grammar.typed-jsonb/infer-jsonb-arg-access-shape :added "4.1"}
(fact "infer-jsonb-arg-access-shape infers keys from js-select"
  (let [fn-def {:body-meta
                {:raw-body '((fu/js-select m (js ["a" "b" "c"])))}} ; close maps
        shape (jsonb/infer-jsonb-arg-access-shape 'm fn-def)]
    (types/jsonb-shape? shape) => true
    (contains? (:fields shape) :a) => true
    (contains? (:fields shape) :b) => true
    (contains? (:fields shape) :c) => true))


^{:refer rt.postgres.grammar.typed-jsonb/symbol->field-key :added "4.1"}
(fact "symbol->field-key converts symbols to field keywords"
  ;; Simple symbol
  (jsonb/symbol->field-key 'm-name) => :name

  ;; With prefix removed
  (jsonb/symbol->field-key 'o-profile) => :profile
  (jsonb/symbol->field-key 'v-account) => :account

  ;; Non-symbol returns nil
  (jsonb/symbol->field-key "string") => nil
  (jsonb/symbol->field-key :keyword) => nil
  (jsonb/symbol->field-key nil) => nil)

^{:refer rt.postgres.grammar.typed-jsonb/field-info :added "4.1"}
(fact "field-info creates field info maps"
  ;; With just type
  (jsonb/field-info :uuid)
  => {:type :uuid :nullable? true}

  ;; With options
  (jsonb/field-info :text {:nullable? false :source "user.name"})
  => {:type :text :nullable? false :source "user.name"}

  ;; Default type when nil
  (jsonb/field-info nil)
  => {:type :jsonb :nullable? true})

^{:refer rt.postgres.grammar.typed-jsonb/typed-binding-form? :added "4.1"}
(fact "typed-binding-form? checks for typed destructuring forms"
  ;; Valid typed form [type symbol]
  (jsonb/typed-binding-form? '(:uuid v-id)) => true
  (jsonb/typed-binding-form? '(:text v-name)) => true

  ;; Invalid forms
  (jsonb/typed-binding-form? 'v-symbol) => false
  (jsonb/typed-binding-form? "string") => false
  (jsonb/typed-binding-form? :keyword) => false
  (jsonb/typed-binding-form? nil) => false
  (jsonb/typed-binding-form? ['vector]) => false)

^{:refer rt.postgres.grammar.typed-jsonb/accessor-expr? :added "4.1"}
(fact "accessor-expr? identifies JSONB accessor expressions"
  ;; :-> operator
  (jsonb/accessor-expr? '(:-> m "data")) => true

  ;; :->> operator
  (jsonb/accessor-expr? '(:->> m "name")) => true

  ;; pg/field-id
  (jsonb/accessor-expr? '(pg/field-id m "org")) => true

  ;; Non-accessor forms
  (jsonb/accessor-expr? '(other-fn x)) => false
  (jsonb/accessor-expr? :keyword) => false
  (jsonb/accessor-expr? nil) => false)

^{:refer rt.postgres.grammar.typed-jsonb/append-path :added "4.1"}
(fact "append-path adds segment to JSONB path"
  (let [path (types/make-jsonb-path [:a :b] 'm)]
    ;; Appends segment
    (let [appended (jsonb/append-path path :c)]
      (:segments appended) => [:a :b :c]
      (:root-var appended) => 'm)

    ;; Nil path returns nil
    (jsonb/append-path nil :c) => nil

    ;; Nil segment handled
    (jsonb/append-path path nil) => nil))

^{:refer rt.postgres.grammar.typed-jsonb/access-descriptors :added "4.1"}
(fact "access-descriptors generates descriptors for accessor expressions"
  (let [ctx (types/make-context {'m :jsonb}
                                {}
                                {'m (types/make-jsonb-path [] 'm)})]
    ;; :-> accessor
    (let [descriptors (jsonb/access-descriptors ctx '(:-> m "data"))]
      (some? descriptors) => true
      (:field-info (first descriptors)) => {:type :jsonb :nullable? true})

    ;; :->> accessor
    (let [descriptors (jsonb/access-descriptors ctx '(:->> m "name"))]
      (some? descriptors) => true
      (:field-info (first descriptors)) => {:type :text :nullable? true})))

^{:refer rt.postgres.grammar.typed-jsonb/access-descriptor :added "4.1"}
(fact "access-descriptor returns single descriptor for expression"
  (let [ctx (types/make-context {'m :jsonb}
                                {}
                                {'m (types/make-jsonb-path [] 'm)})]
    ;; Returns first descriptor
    (let [desc (jsonb/access-descriptor ctx '(:-> m "id"))]
      (some? desc) => true
      (:field-info desc) => {:type :jsonb :nullable? true})

    ;; Nil for non-accessor
    (jsonb/access-descriptor ctx '(other-fn x)) => nil))

^{:refer rt.postgres.grammar.typed-jsonb/expr-jsonb-path :added "4.1"}
(fact "expr-jsonb-path extracts JSONB path from expression"
  (let [path (types/make-jsonb-path [:a] 'm)]
    ;; Returns path directly
    (jsonb/expr-jsonb-path {} path) => path

    ;; Looks up symbol in context
    (let [ctx (types/make-context {} {} {'m path})]
      (jsonb/expr-jsonb-path ctx 'm) => path)

    ;; Extracts from accessor
    (let [ctx (types/make-context {'m :jsonb} {} {'m (types/make-jsonb-path [] 'm)})]
      (:segments (jsonb/expr-jsonb-path ctx '(:-> m "data"))) => [:data])))

^{:refer rt.postgres.grammar.typed-jsonb/set-binding-descriptors :added "4.1"}
(fact "set-binding-descriptors generates descriptors for set destructuring"
  (let [source-path (types/make-jsonb-path [:data] 'm)]
    ;; Symbol binding
    (let [descriptors (jsonb/set-binding-descriptors source-path '#{o-profile})]
      (count descriptors) => 1
      (:var (first descriptors)) => 'o-profile)

    ;; Typed binding form
    (let [descriptors (jsonb/set-binding-descriptors source-path '#{(:uuid v-id)})]
      (count descriptors) => 1
      (:var (first descriptors)) => 'v-id
      (get-in (first descriptors) [:field-info :type]) => :uuid)))

^{:refer rt.postgres.grammar.typed-jsonb/binding-descriptors :added "4.1"}
(fact "binding-descriptors generates descriptors for binding forms"
  (let [ctx (types/make-context {'m :jsonb} {} {'m (types/make-jsonb-path [] 'm)})]
    ;; Symbol binding to accessor
    (let [descriptors (jsonb/binding-descriptors ctx 'n '(:-> m "id"))]
      (some? descriptors) => true
      (:var (first descriptors)) => 'n)

    ;; Set binding
    (let [descriptors (jsonb/binding-descriptors ctx '#{o-data} 'm)]
      (some? descriptors) => true)

    ;; Typed binding form
    (let [descriptors (jsonb/binding-descriptors ctx '(:uuid v-id) 'm)]
      (some? descriptors) => true
      (:var (first descriptors)) => 'v-id)))

^{:refer rt.postgres.grammar.typed-jsonb/descriptor-shape :added "4.1"}
(fact "descriptor-shape creates shape from descriptor path"
  ;; Single segment path
  (let [path (types/make-jsonb-path [:id] 'm)
        descriptor {:path path :field-info {:type :uuid}}
        shape (jsonb/descriptor-shape descriptor)]
    (types/jsonb-shape? shape) => true
    (get-in shape [:fields :id :type]) => :uuid)

  ;; Nested path
  (let [path (types/make-jsonb-path [:profile :name] 'm)
        descriptor {:path path :field-info {:type :text}}
        shape (jsonb/descriptor-shape descriptor)]
    (types/jsonb-shape? shape) => true
    (get-in shape [:fields :profile :shape :fields :name :type]) => :text)

  ;; Empty path returns nil
  (jsonb/descriptor-shape {:path (types/make-jsonb-path [] 'm) :field-info {}}) => nil)

^{:refer rt.postgres.grammar.typed-jsonb/update-root-shape :added "4.1"}
(fact "update-root-shape adds descriptor shape to context"
  (let [ctx (types/make-context {'m :jsonb} {} {'m (types/make-jsonb-path [] 'm)})
        path (types/make-jsonb-path [:id] 'm)
        descriptor {:path path :field-info {:type :uuid}}]
    ;; Updates shape
    (let [updated (jsonb/update-root-shape ctx descriptor)]
      (some? (get-in updated [:jsonb-shapes 'm])) => true))

  ;; Nil descriptor returns unchanged
  (let [ctx (types/make-context {})]
    (jsonb/update-root-shape ctx nil) => ctx))

^{:refer rt.postgres.grammar.typed-jsonb/apply-descriptor :added "4.1"}
(fact "apply-descriptor adds descriptor info to context"
  (let [ctx (types/make-context {'m :jsonb} {} {'m (types/make-jsonb-path [] 'm)})
        path (types/make-jsonb-path [:id] 'm)
        descriptor {:var 'v-id
                    :path path
                    :binding-type {:kind :cast :type :uuid}
                    :field-info {:type :uuid}}]
    ;; Applies binding
    (let [updated (jsonb/apply-descriptor ctx descriptor)]
      (get-in updated [:bindings 'v-id]) => {:kind :cast :type :uuid})))

^{:refer rt.postgres.grammar.typed-jsonb/apply-descriptors :added "4.1"}
(fact "apply-descriptors applies multiple descriptors"
  (let [ctx (types/make-context {'m :jsonb} {} {'m (types/make-jsonb-path [] 'm)})
        path1 (types/make-jsonb-path [:id] 'm)
        path2 (types/make-jsonb-path [:name] 'm)
        descriptors [{:var 'v-id
                      :path path1
                      :binding-type {:kind :cast :type :uuid}
                      :field-info {:type :uuid}}
                     {:var 'v-name
                      :path path2
                      :binding-type {:kind :cast :type :text}
                      :field-info {:type :text}}]]
    ;; Applies all descriptors
    (let [updated (jsonb/apply-descriptors ctx descriptors)]
      (contains? (:bindings updated) 'v-id) => true
      (contains? (:bindings updated) 'v-name) => true)

    ;; Empty descriptors returns unchanged
    (jsonb/apply-descriptors ctx []) => ctx))

^{:refer rt.postgres.grammar.typed-jsonb/js-keys-form->keywords :added "4.1"}
(fact "js-keys-form->keywords extracts keywords from js forms"
  ;; From vector
  (jsonb/js-keys-form->keywords '["id" "name" "email"])
  => [:id :name :email]

  ;; From (js [...]) form
  (jsonb/js-keys-form->keywords '(js ["a" "b"]))
  => [:a :b]

  ;; Mixed keywords and strings
  (jsonb/js-keys-form->keywords '["id" :name "data"])
  => [:id :name :data]

  ;; Invalid form returns nil
  (jsonb/js-keys-form->keywords "string") => nil
  (jsonb/js-keys-form->keywords nil) => nil)

^{:refer rt.postgres.grammar.typed-jsonb/js-select-descriptors :added "4.1"}
(fact "js-select-descriptors generates descriptors for js-select calls"
  (let [ctx (types/make-context {'m :jsonb} {} {'m (types/make-jsonb-path [] 'm)})]
    ;; Valid js-select form
    (let [descriptors (jsonb/js-select-descriptors ctx '(js-select m (js ["id" "name"])))]
      (count descriptors) => 2
      (:field-info (first descriptors)) => {:type :jsonb :nullable? true})

    ;; Seeded table shape preserves concrete column types
    (let [shape (types/make-jsonb-shape {:id {:type :uuid :nullable? false}
                                         :name {:type :text :nullable? true}}
                                        "User")
          seeded-ctx (types/make-context {'m :jsonb}
                                         {'m shape}
                                         {'m (types/make-jsonb-path [] 'm)})
          descriptors (jsonb/js-select-descriptors seeded-ctx '(js-select m (js ["id" "name"])))]
      (:field-info (first descriptors)) => {:type :uuid :nullable? false}
      (:field-info (second descriptors)) => {:type :text :nullable? true})

    ;; With vector directly
    (let [descriptors (jsonb/js-select-descriptors ctx '(js-select m ["id"]))]
      (count descriptors) => 1)

    ;; Non-js-select returns nil
    (jsonb/js-select-descriptors ctx '(other-fn m ["id"])) => nil))

^{:refer rt.postgres.grammar.typed-jsonb/analyze-binding :added "4.1"}
(fact "analyze-binding processes a single binding pair"
  (let [ctx (types/make-context {'m :jsonb} {} {'m (types/make-jsonb-path [] 'm)})]
    ;; Simple binding
    (let [result (jsonb/analyze-binding ctx '[n (:-> m "id")])]
      (contains? (:bindings result) 'n) => true)))

^{:refer rt.postgres.grammar.typed-jsonb/scan-form :added "4.1"}
(fact "scan-form accumulates binding info from form traversal"
  (let [ctx (types/make-context {'m :jsonb} {} {'m (types/make-jsonb-path [] 'm)})]
    ;; Scans accessor expression
    (let [result (jsonb/scan-form ctx '(:-> m "id"))]
      (some? result) => true)

    ;; Scans let form
    (let [result (jsonb/scan-form ctx '(let [n (:-> m "id")] n))]
      (contains? (:bindings result) 'n) => true)

    ;; Returns context unchanged for non-seq
    (let [ctx (types/make-context {})]
      (jsonb/scan-form ctx 'symbol) => ctx)))


^{:refer rt.postgres.grammar.typed-jsonb/source-root-shape :added "4.1"}
(fact "source-root-shape returns the root jsonb shape for a path"
  (let [shape (types/make-jsonb-shape
               {:id {:type :uuid :nullable? false}}
               "User")
        ctx (types/make-context {'m :jsonb}
                                {'m shape}
                                {'m (types/make-jsonb-path [] 'm)})]
    (types/jsonb-shape? (jsonb/source-root-shape ctx (types/make-jsonb-path [:id] 'm))) => true))

^{:refer rt.postgres.grammar.typed-jsonb/source-field-info :added "4.1"}
(fact "source-field-info uses source shapes when available"
  (let [shape (types/make-jsonb-shape
               {:id {:type :uuid :nullable? false}}
               "User")
        ctx (types/make-context {'m :jsonb}
                                {'m shape}
                                {'m (types/make-jsonb-path [] 'm)})
        path (types/make-jsonb-path [:id] 'm)]
    (jsonb/source-field-info ctx path :id :->) => {:type :uuid :nullable? false}
    (jsonb/source-field-info ctx path :missing :->>) => {:type :text :nullable? true}))

^{:refer rt.postgres.grammar.typed-jsonb/js-select-shape :added "4.1"}
(fact "js-select-shape projects selected fields from source shapes"
  (let [shape (types/make-jsonb-shape
               {:id {:type :uuid :nullable? false}
                :name {:type :text :nullable? true}}
               "User")
        ctx (types/make-context {'m :jsonb}
                                {'m shape}
                                {'m (types/make-jsonb-path [] 'm)})]
    (types/jsonb-shape? (jsonb/js-select-shape ctx '(js-select m (js ["id" "name"])))) => true
    (get-in (jsonb/js-select-shape ctx '(js-select m (js ["id" "name"])))
            [:fields :id :type]) => :uuid
    (get-in (jsonb/js-select-shape ctx '(js-select m (js ["id" "name"])))
            [:fields :name :type]) => :text))
