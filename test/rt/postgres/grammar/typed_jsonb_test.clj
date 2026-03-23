(ns rt.postgres.grammar.typed-jsonb-test
  (:use code.test)
  (:require [rt.postgres.grammar.typed-common :as types]
            [rt.postgres.grammar.typed-jsonb :refer :all]
            [rt.postgres.grammar.typed-parse :as parse]))

(defn make-root-ctx
  []
  (types/make-context {'m :jsonb}
                      {}
                      {'m (types/make-jsonb-path [] 'm)}))

^{:refer rt.postgres.grammar.typed-jsonb/symbol->field-key :added "4.1"}
(fact "symbol->field-key strips common binding prefixes"
  (symbol->field-key 'v-name) => :name
  (symbol->field-key 'o-profile) => :profile
  (symbol->field-key 'plain) => :plain
  (symbol->field-key :not-a-symbol) => nil)

^{:refer rt.postgres.grammar.typed-jsonb/field-info :added "4.1"}
(fact "field-info defaults nullability and merges extra options"
  (field-info :uuid) => {:type :uuid :nullable? true}
  (field-info nil {:nullable? false :shape :nested}) => {:type :jsonb
                                                         :nullable? false
                                                         :shape :nested})

^{:refer rt.postgres.grammar.typed-jsonb/typed-binding-form? :added "4.1"}
(fact "typed-binding-form? recognizes typed destructuring entries"
  (typed-binding-form? '(:text v-name)) => true
  (typed-binding-form? 'v-name) => false
  (typed-binding-form? '(:text "not-a-symbol")) => false)

^{:refer rt.postgres.grammar.typed-jsonb/accessor-expr? :added "4.1"}
(fact "accessor-expr? recognizes jsonb access forms"
  (boolean (accessor-expr? '(:-> m "profile"))) => true
  (boolean (accessor-expr? '(pg/field-id m "organisation"))) => true
  (boolean (accessor-expr? '(str m))) => false)

^{:refer rt.postgres.grammar.typed-jsonb/append-path :added "4.1"}
(fact "append-path extends a JsonbPath"
  (append-path (types/make-jsonb-path [:profile] 'm) :name)
  => {:segments [:profile :name]
      :root-var 'm}

  (append-path nil :name) => nil)

^{:refer rt.postgres.grammar.typed-jsonb/access-descriptor :added "4.1"}
(fact "access-descriptor normalizes jsonb field access into path descriptors"
  (let [ctx (make-root-ctx)]
    (get-in (access-descriptor ctx '(:-> m "profile")) [:path :segments]) => [:profile]
    (get-in (access-descriptor ctx '(:-> m "profile")) [:path :root-var]) => 'm
    (get-in (access-descriptor ctx '(:-> m "profile")) [:field-info :type]) => :jsonb

    (get-in (access-descriptor ctx '(pg/field-id m "organisation")) [:path :segments]) => [:organisation]
    (get-in (access-descriptor ctx '(pg/field-id m "organisation")) [:field-info :type]) => :uuid))

^{:refer rt.postgres.grammar.typed-jsonb/expr-jsonb-path :added "4.1"}
(fact "expr-jsonb-path resolves direct symbols and nested accessor paths"
  (let [ctx (make-root-ctx)]
    (expr-jsonb-path ctx 'm) => {:segments [] :root-var 'm}
    (expr-jsonb-path ctx '(:-> m "profile")) => {:segments [:profile] :root-var 'm}
    (expr-jsonb-path ctx 'missing) => nil))

^{:refer rt.postgres.grammar.typed-jsonb/binding-descriptors :added "4.1"}
(fact "binding-descriptors describes typed and plain jsonb destructuring bindings"
  (let [ctx    (make-root-ctx)
        descs  (binding-descriptors ctx '#{(:text v-name) o-profile} 'm)
        by-var (into {} (map (juxt :var identity)) descs)]
    (get-in by-var ['v-name :path :segments]) => [:name]
    (get-in by-var ['v-name :binding-type]) => {:kind :cast :type :text}
    (get-in by-var ['o-profile :path :segments]) => [:profile]
    (get-in by-var ['o-profile :binding-type]) => :jsonb)

  (let [ctx (make-root-ctx)]
    (let [desc (first (binding-descriptors ctx 'o-profile '(:-> m "profile")))]
      (:var desc) => 'o-profile
      (get-in desc [:path :segments]) => [:profile]
      (:binding-type desc) => :jsonb
      (get-in desc [:field-info :type]) => :jsonb)))

^{:refer rt.postgres.grammar.typed-jsonb/descriptor-shape :added "4.1"}
(fact "descriptor-shape creates a nested shape for a descriptor path"
  (let [shape (descriptor-shape {:path (types/make-jsonb-path [:profile :name] 'm)
                                 :field-info (field-info :text)})]
    (get-in shape [:fields :profile :shape :fields :name :type]) => :text))

^{:refer rt.postgres.grammar.typed-jsonb/update-root-shape :added "4.1"}
(fact "update-root-shape merges descriptor shape into the root variable shape"
  (let [ctx     (make-root-ctx)
        desc    {:path (types/make-jsonb-path [:profile :name] 'm)
                 :field-info (field-info :text)}
        updated (update-root-shape ctx desc)]
    (get-in (types/get-var-shape updated 'm)
            [:fields :profile :shape :fields :name :type])
    => :text))

^{:refer rt.postgres.grammar.typed-jsonb/apply-descriptor :added "4.1"}
(fact "apply-descriptor stores the binding, path, and updated root shape"
  (let [ctx  (make-root-ctx)
        desc {:var 'o-profile
              :path (types/make-jsonb-path [:profile] 'm)
              :binding-type :jsonb
              :field-info (field-info :jsonb)}
        out  (apply-descriptor ctx desc)]
    (types/lookup-binding out 'o-profile) => :jsonb
    (types/get-var-path out 'o-profile) => {:segments [:profile] :root-var 'm}
    (get-in (types/get-var-shape out 'm) [:fields :profile :type]) => :jsonb))

^{:refer rt.postgres.grammar.typed-jsonb/apply-descriptors :added "4.1"}
(fact "apply-descriptors applies multiple jsonb binding descriptors"
  (let [ctx  (make-root-ctx)
        out  (apply-descriptors
              ctx
              [{:var 'o-profile
                :path (types/make-jsonb-path [:profile] 'm)
                :binding-type :jsonb
                :field-info (field-info :jsonb)}
               {:var 'v-name
                :path (types/make-jsonb-path [:profile :name] 'm)
                :binding-type {:kind :cast :type :text}
                :field-info (field-info :text)}])]
    (types/lookup-binding out 'v-name) => {:kind :cast :type :text}
    (types/get-var-path out 'v-name) => {:segments [:profile :name] :root-var 'm}
    (get-in (types/get-var-shape out 'm)
            [:fields :profile :shape :fields :name :type])
    => :text))

^{:refer rt.postgres.grammar.typed-jsonb/scan-form :added "4.1"}
(fact "scan-form tracks destructured jsonb fields and field-id access"
  (let [ctx     (make-root-ctx)
        form    '(let [#{(:text v-code)
                         o-profile} m
                       (:uuid v-id) (pg/field-id m "id")]
                   (return o-profile))
        scanned (scan-form ctx form)
        shape   (types/get-var-shape scanned 'm)]
    (-> shape :fields keys set) => #{:code :profile :id}
    (get-in shape [:fields :code :type]) => :text
    (get-in shape [:fields :id :type]) => :uuid
    (types/get-var-path scanned 'o-profile) => {:segments [:profile] :root-var 'm}))

^{:refer rt.postgres.grammar.typed-jsonb/infer-jsonb-arg-access-shape :added "4.1"}
(fact "infer-jsonb-arg-access-shape returns the accessed jsonb field shape"
  (let [form '(defn.pg
                prepare-topic
                "prepares a topic payload"
                [:jsonb m]
                (let [(:uuid v-organisation-id) (pg/field-id m "organisation")
                      #{(:text v-code)
                        (:text v-format)
                        (:citext v-currency-id)} m]
                  (return
                   (|| {:publish "none"}
                       m
                       {:code-full v-code
                        :organisation-id v-organisation-id
                        :format v-format
                        :currency-id v-currency-id}))))
        fn-def (parse/parse-defn form "test.ns" nil)
        shape  (infer-jsonb-arg-access-shape 'm fn-def)]
    (-> shape :fields keys set) => #{:organisation :code :format :currency-id}
    (get-in shape [:fields :organisation :type]) => :uuid
    (get-in shape [:fields :currency-id :type]) => :citext
    (:confidence shape) => :medium))
