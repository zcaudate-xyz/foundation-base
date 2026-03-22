(ns rt.postgres.grammar.typed-common-test
  (:require [rt.postgres.grammar.typed-common :as types])
  (:use code.test))

;; -----------------------------------------------------------------------------
;; Type Predicates
;; -----------------------------------------------------------------------------

^{:refer rt.postgres.grammar.typed-common/type-ref? :added "0.1"}
(fact "type-ref? returns true for TypeRef records"
  (types/type-ref? (types/make-type-ref :primitive nil :uuid)) => true
  (types/type-ref? :uuid) => false
  (types/type-ref? nil) => false
  (types/type-ref? {}) => false)

^{:refer rt.postgres.grammar.typed-common/table-def? :added "0.1"}
(fact "table-def? returns true for TableDef records"
  (types/table-def? (types/make-table-def :core "User" [] :id)) => true
  (types/table-def? {}) => false
  (types/table-def? nil) => false)

^{:refer rt.postgres.grammar.typed-common/enum-def? :added "0.1"}
(fact "enum-def? returns true for EnumDef records"
  (types/enum-def? (types/make-enum-def :test "Status" #{:active} nil)) => true
  (types/enum-def? {}) => false
  (types/enum-def? nil) => false)

^{:refer rt.postgres.grammar.typed-common/fn-def? :added "0.1"}
(fact "fn-def? returns true for FnDef records"
  (types/fn-def? (types/make-fn-def :core "get-user" [] :jsonb {} nil)) => true
  (types/fn-def? {}) => false
  (types/fn-def? nil) => false)

^{:refer rt.postgres.grammar.typed-common/jsonb-shape? :added "0.1"}
(fact "jsonb-shape? returns true for JsonbShape records"
  (types/jsonb-shape? (types/make-jsonb-shape {"id" :uuid})) => true
  (types/jsonb-shape? {}) => false
  (types/jsonb-shape? nil) => false)

^{:refer rt.postgres.grammar.typed-common/jsonb-merge? :added "0.1"}
(fact "jsonb-merge? returns true for JsonbMerge records"
  (let [s1 (types/make-jsonb-shape {"id" :uuid})
        s2 (types/make-jsonb-shape {"name" :text})]
    (types/jsonb-merge? (types/make-jsonb-merge s1 s2)) => true
    (types/jsonb-merge? s1) => false))

^{:refer rt.postgres.grammar.typed-common/jsonb-array? :added "0.1"}
(fact "jsonb-array? returns true for JsonbArray records"
  (types/jsonb-array? (types/make-jsonb-array :uuid)) => true
  (types/jsonb-array? []) => false
  (types/jsonb-array? nil) => false)

^{:refer rt.postgres.grammar.typed-common/type-union? :added "0.1"}
(fact "type-union? returns true for TypeUnion records"
  (types/type-union? (types/make-type-union [:uuid :text])) => true
  (types/type-union? #{:uuid :text}) => false
  (types/type-union? nil) => false)

^{:refer rt.postgres.grammar.typed-common/binding-context? :added "0.1"}
(fact "binding-context? returns true for BindingContext records"
  (types/binding-context? (types/make-context)) => true
  (types/binding-context? {:bindings {}}) => false
  (types/binding-context? nil) => false)

^{:refer rt.postgres.grammar.typed-common/primitive? :added "0.1"}
(fact "primitive? returns true for primitive type keywords"
  (types/primitive? :uuid) => true
  (types/primitive? :text) => true
  (types/primitive? :jsonb) => true
  (types/primitive? :integer) => true
  (types/primitive? :boolean) => true
  (types/primitive? :unknown) => false
  (types/primitive? nil) => false)

^{:refer rt.postgres.grammar.typed-common/table? :added "0.1"}
(fact "table? returns true for table type refs"
  (types/table? (types/make-type-ref :table :core "User")) => true
  (types/table? (types/make-type-ref :enum :core "Status")) => false
  (types/table? :table) => false)

^{:refer rt.postgres.grammar.typed-common/enum? :added "0.1"}
(fact "enum? returns true for enum type refs"
  (types/enum? (types/make-type-ref :enum :core "Status")) => true
  (types/enum? (types/make-type-ref :table :core "User")) => false
  (types/enum? :enum) => false)

^{:refer rt.postgres.grammar.typed-common/ref? :added "0.1"}
(fact "ref? returns true for ref type refs"
  (types/ref? (types/make-type-ref :ref nil "User")) => true
  (types/ref? (types/make-type-ref :table :core "User")) => false
  (types/ref? :ref) => false)

;; -----------------------------------------------------------------------------
;; Registry Operations
;; -----------------------------------------------------------------------------

^{:refer rt.postgres.grammar.typed-common/register-type! :added "0.1"}
(fact "register-type! and get-type manage the type registry with namespaced keys"
  (types/clear-registry!)
  (let [t (types/make-type-ref :table :core "User")]
    (types/register-type! 'core/User t)
    (types/get-type 'core/User) => t)
  (types/clear-registry!))

^{:refer rt.postgres.grammar.typed-common/register-type! :added "0.1"}
(fact "register-type! validates that keys are namespaced symbols"
  ;; Valid namespaced symbols should work
  (types/register-type! 'test/ValidType (types/make-type-ref :primitive nil :test))
  => anything

  ;; Non-symbol keys should throw
  (types/register-type! :keyword-key (types/make-type-ref :primitive nil :test))
  => (throws clojure.lang.ExceptionInfo)

  ;; Unqualified symbols should throw
  (types/register-type! 'unqualified (types/make-type-ref :primitive nil :test))
  => (throws clojure.lang.ExceptionInfo)

  ;; String keys should throw
  (types/register-type! "string-key" (types/make-type-ref :primitive nil :test))
  => (throws clojure.lang.ExceptionInfo)

  ;; Empty namespace should throw
  (types/register-type! (symbol "" "name") (types/make-type-ref :primitive nil :test))
  => (throws clojure.lang.ExceptionInfo)

  ;; Cleanup
  (types/clear-registry!))

^{:refer rt.postgres.grammar.typed-common/get-type :added "0.1"}
(fact "get-type returns nil for non-existent types"
  (types/clear-registry!)
  (types/get-type 'NonExistentType) => nil)

^{:refer rt.postgres.grammar.typed-common/clear-registry! :added "0.1"}
(fact "clear-registry! empties the type registry"
  (types/clear-registry!)
  (types/register-type! 'test/TestType (types/make-type-ref :primitive nil :test))
  (types/clear-registry!)
  (types/get-type 'test/TestType) => nil)

;; -----------------------------------------------------------------------------
;; App Typed Payload
;; -----------------------------------------------------------------------------

^{:refer rt.postgres.grammar.typed-common/type-key :added "4.1"}
(fact "type-key returns stable symbol keys for typed defs"
  (types/type-key (types/make-table-def "test.ns" "User" [] :id))
  => 'test.ns/User
  (types/type-key (types/make-enum-def nil "Status" #{:active} nil))
  => 'Status)

^{:refer rt.postgres.grammar.typed-common/empty-typed :added "4.1"}
(fact "empty-typed returns the canonical empty payload"
  (types/empty-typed)
  => {:tables {}
      :enums {}
      :functions {}})

^{:refer rt.postgres.grammar.typed-common/analysis->typed :added "4.1"}
(fact "analysis->typed normalizes analysis vectors into keyed maps"
  (let [payload (types/analysis->typed
                 {:tables [(types/make-table-def "test.ns" "User" [] :id)]
                  :enums [(types/make-enum-def "test.ns" "Status" #{:active} nil)]
                  :functions [(types/make-fn-def "test.ns" "create-user" [] :jsonb {} nil)]})]
    (keys (:tables payload)) => '(test.ns/User)
    (keys (:enums payload)) => '(test.ns/Status)
    (keys (:functions payload)) => '(test.ns/create-user)))

^{:refer rt.postgres.grammar.typed-common/merge-typed :added "4.1"}
(fact "merge-typed merges app payload fragments by category"
  (types/merge-typed {:tables {'a/Table :table} :enums {} :functions {}}
                     {:tables {} :enums {'a/Status :enum} :functions {'a/do-work :fn}})
  => {:tables {'a/Table :table}
      :enums {'a/Status :enum}
      :functions {'a/do-work :fn}})

;; -----------------------------------------------------------------------------
;; Type Constructors
;; -----------------------------------------------------------------------------

^{:refer rt.postgres.grammar.typed-common/make-type-ref :added "0.1"}
(fact "make-type-ref creates TypeRef with various arities"
  ;; arity 1 - just kind
  (let [t1 (types/make-type-ref :primitive)]
    (:kind t1) => :primitive
    (:ns t1) => nil
    (:name t1) => nil)
  ;; arity 3 - kind, ns, name
  (let [t2 (types/make-type-ref :table :core "User")]
    (:kind t2) => :table
    (:ns t2) => :core
    (:name t2) => "User")
  ;; arity 4 - with constraints
  (let [t3 (types/make-type-ref :enum :types "Status" {:values #{"active" "inactive"}})]
    (:kind t3) => :enum
    (:constraints t3) => {:values #{"active" "inactive"}}))

^{:refer rt.postgres.grammar.typed-common/make-enum-def :added "0.1"}
(fact "make-enum-def creates EnumDef with set of values"
  (let [e (types/make-enum-def :test "Status" #{:active :inactive} nil)]
    (:ns e) => :test
    (:name e) => "Status"
    (:values e) => #{:active :inactive})
  ;; ensures values is a set
  (let [e (types/make-enum-def :test "Type" [:a :b :c] nil)]
    (:values e) => #{:a :b :c}))

^{:refer rt.postgres.grammar.typed-common/make-column-def :added "0.1"}
(fact "make-column-def creates ColumnDef with various arities"
  ;; arity 2 - name and type
  (let [c (types/make-column-def :handle :citext)]
    (:name c) => :handle
    (:type c) => :citext)
  ;; arity 3 - with options
  (let [c (types/make-column-def :email :text {:required true :unique true})]
    (:name c) => :email
    (:type c) => :text
    (:required c) => true
    (:unique c) => true))

^{:refer rt.postgres.grammar.typed-common/make-table-def :added "0.1"}
(fact "make-table-def creates TableDef with various arities"
  ;; arity 4 - basic table
  (let [columns [(types/make-column-def :id :uuid)
                 (types/make-column-def :name :text)]
        t (types/make-table-def :core "User" columns :id)]
    (:ns t) => :core
    (:name t) => "User"
    (:columns t) => columns
    (:primary-key t) => :id)
  ;; arity 6 - with addons and meta
  (let [t (types/make-table-def :core "User" [] :id {:audit true} {:entity :user} nil)]
    (:addons t) => {:audit true}
    (:entity-meta t) => {:entity :user}))

^{:refer rt.postgres.grammar.typed-common/make-fn-def :added "0.1"}
(fact "make-fn-def creates FnDef"
  (let [f (types/make-fn-def :core "get-user" [:uuid :jsonb] :jsonb {:async true} nil)]
    (:ns f) => :core
    (:name f) => "get-user"
    (:inputs f) => [:uuid :jsonb]
    (:output f) => :jsonb
    (:body-meta f) => {:async true}))

^{:refer rt.postgres.grammar.typed-common/make-jsonb-shape :added "0.1"}
(fact "make-jsonb-shape creates shape with unified fields and various arities"
  ;; arity 1 - fields only
  (let [s (types/make-jsonb-shape {"id" :uuid})]
    (:fields s) => {"id" :uuid}
    (:source-table s) => nil
    (:confidence s) => :medium
    (:nullable? s) => false)
  ;; arity 2 - with source table
  (let [s (types/make-jsonb-shape {"id" :uuid} :literal)]
    (:source-table s) => :literal)
  ;; arity 3 - with confidence
  (let [s (types/make-jsonb-shape {"id" :uuid} :User :high)]
    (:confidence s) => :high)
  ;; arity 4 - full specification
  (let [s (types/make-jsonb-shape {"id" :uuid} :User :high true)]
    (:nullable? s) => true))

^{:refer rt.postgres.grammar.typed-common/make-jsonb-merge :added "0.1"}
(fact "make-jsonb-merge creates JsonbMerge from two shapes"
  (let [s1 (types/make-jsonb-shape {"id" :uuid} :User)
        s2 (types/make-jsonb-shape {"name" :text} :Profile)
        m (types/make-jsonb-merge s1 s2)]
    (:left m) => s1
    (:right m) => s2))

^{:refer rt.postgres.grammar.typed-common/make-jsonb-array :added "0.1"}
(fact "make-jsonb-array creates JsonbArray"
  (let [a (types/make-jsonb-array :uuid)]
    (:element-type a) => :uuid)
  (let [a (types/make-jsonb-array (types/make-type-ref :table nil "User"))]
    (:name (:element-type a)) => "User"))

^{:refer rt.postgres.grammar.typed-common/make-type-union :added "0.1"}
(fact "make-type-union creates TypeUnion with deduplicated types"
  (let [u (types/make-type-union [:uuid :text])]
    (set (:types u)) => #{:uuid :text})
  ;; deduplication
  (let [u (types/make-type-union [:uuid :uuid :text])]
    (count (:types u)) => 2))

^{:refer rt.postgres.grammar.typed-common/make-jsonb-path :added "0.1"}
(fact "make-jsonb-path creates path with various arities"
  (types/make-jsonb-path ["data" "items"])
  => (contains {:segments ["data" "items"] :root-var nil})
  (types/make-jsonb-path ["data" "items"] 'input)
  => (contains {:segments ["data" "items"] :root-var 'input}))

^{:refer rt.postgres.grammar.typed-common/make-jsonb-inference :added "0.1"}
(fact "make-jsonb-inference creates JsonbInference"
  (let [i (types/make-jsonb-inference {:fields {}} {:x :uuid} #{:merge})]
    (:return-shape i) => {:fields {}}
    (:intermediate-vars i) => {:x :uuid}
    (:operations-detected i) => #{:merge}))

^{:refer rt.postgres.grammar.typed-common/make-context :added "0.1"}
(fact "make-context creates BindingContext with various arities"
  ;; arity 0
  (types/make-context) => (contains {:bindings {} :jsonb-shapes {} :jsonb-paths {} :parent nil})
  ;; arity 1
  (types/make-context {:x :uuid}) => (contains {:bindings {:x :uuid}})
  ;; arity 2
  (types/make-context {:x :uuid} {:x {:fields {}}}) => (contains {:bindings {:x :uuid} :jsonb-shapes {:x {:fields {}}}})
  ;; arity 3
  (types/make-context {:x :uuid} {:x {:fields {}}} {:x {:segments ["x"]}})
  => (contains {:bindings {:x :uuid} :jsonb-shapes {:x {:fields {}}} :jsonb-paths {:x {:segments ["x"]}}}))

;; -----------------------------------------------------------------------------
;; Shape Operations
;; -----------------------------------------------------------------------------

^{:refer rt.postgres.grammar.typed-common/empty-jsonb-shape :added "0.1"}
(fact "empty-jsonb-shape creates empty shape with low confidence"
  (types/empty-jsonb-shape) => (contains {:fields {} :source-table nil :confidence :low :nullable? false}))

^{:refer rt.postgres.grammar.typed-common/add-key :added "0.1"}
(fact "add-key adds a key with its type to shape"
  (let [shape (types/make-jsonb-shape {"id" :uuid})]
    (types/add-key shape "name" :text))
  => (contains {:fields {"id" :uuid "name" :text}})
  (let [shape (types/empty-jsonb-shape)]
    (types/add-key shape "status" :boolean))
  => (contains {:fields {"status" :boolean}}))

^{:refer rt.postgres.grammar.typed-common/get-key-type :added "0.1"}
(fact "get-key-type returns type for existing key or nil"
  (let [shape (types/make-jsonb-shape {"id" :uuid "name" :text})]
    (types/get-key-type shape "id") => :uuid
    (types/get-key-type shape "missing") => nil))

^{:refer rt.postgres.grammar.typed-common/has-key? :added "0.1"}
(fact "has-key? checks if key exists in shape"
  (let [shape (types/make-jsonb-shape {"id" :uuid "name" :text})]
    (types/has-key? shape "id") => true
    (types/has-key? shape "missing") => false))

^{:refer rt.postgres.grammar.typed-common/merge-shapes :added "0.1"}
(fact "merge-shapes combines two shapes and takes minimum confidence"
  ;; basic merge
  (let [shape1 (types/make-jsonb-shape {"id" :uuid} :source1 :high)
        shape2 (types/make-jsonb-shape {"name" :text} :source2 :medium)]
    (types/merge-shapes shape1 shape2))
  => (contains {:fields {"id" :uuid "name" :text} :confidence :medium})
  ;; merging with type conflict creates union
  (let [shape1 (types/make-jsonb-shape {"id" :uuid} :source1 :high)
        shape2 (types/make-jsonb-shape {"id" :text} :source2 :high)
        merged (types/merge-shapes shape1 shape2)]
    (types/type-union? (get-in merged [:fields "id"])) => true
    (:confidence merged) => :medium)
  ;; handles nil shapes
  (let [shape1 (types/make-jsonb-shape {"id" :uuid})]
    (types/merge-shapes shape1 nil) => shape1
    (types/merge-shapes nil shape1) => shape1
    (types/merge-shapes nil nil) => nil))

^{:refer rt.postgres.grammar.typed-common/flatten-shape :added "0.1"}
(fact "flatten-shape merges JsonbShape and JsonbMerge into a single map"
  ;; simple shape
  (let [s (types/make-jsonb-shape {"id" :uuid} :User)]
    (types/flatten-shape s) => {"id" :uuid})
  ;; merge
  (let [s1 (types/make-jsonb-shape {"id" :uuid} :User)
        s2 (types/make-jsonb-shape {"name" :text} :Profile)
        m (types/make-jsonb-merge s1 s2)]
    (types/flatten-shape m) => {"id" :uuid "name" :text})
  ;; map input
  (types/flatten-shape {:fields {"x" :uuid}}) => {"x" :uuid}
  (types/flatten-shape {:x :uuid}) => {:x :uuid}
  (types/flatten-shape nil) => {})

;; -----------------------------------------------------------------------------
;; Context Operations
;; -----------------------------------------------------------------------------

^{:refer rt.postgres.grammar.typed-common/push-scope :added "0.1"}
(fact "push-scope creates a new child context"
  (let [ctx (types/make-context {:x :uuid})]
    (types/push-scope ctx) => (contains {:parent ctx :bindings {}})))

^{:refer rt.postgres.grammar.typed-common/pop-scope :added "0.1"}
(fact "pop-scope returns the parent context"
  (let [parent (types/make-context {:x :uuid})
        child (types/push-scope parent)]
    (types/pop-scope child) => parent))

^{:refer rt.postgres.grammar.typed-common/add-binding :added "0.1"}
(fact "add-binding adds a variable binding to the context"
  (let [ctx (types/make-context)]
    (types/add-binding ctx 'user :uuid))
  => (contains-in {:bindings {'user :uuid}})
  (let [ctx (types/make-context)]
    (types/add-binding ctx 'user :uuid :shape (types/make-jsonb-shape {"id" :uuid})))
  => (contains-in {:bindings {'user :uuid} :jsonb-shapes {'user {:fields {"id" :uuid}}}}))

^{:refer rt.postgres.grammar.typed-common/lookup-binding :added "0.1"}
(fact "lookup-binding finds binding in context or parents"
  (let [ctx (types/add-binding (types/make-context) 'user :uuid)]
    (types/lookup-binding ctx 'user) => :uuid)
  (let [parent (types/add-binding (types/make-context) 'user :uuid)
        child (types/push-scope parent)]
    (types/lookup-binding child 'user) => :uuid)
  (let [ctx (types/make-context)]
    (types/lookup-binding ctx 'missing) => nil))

^{:refer rt.postgres.grammar.typed-common/get-var-shape :added "0.1"}
(fact "get-var-shape finds shape in context or parents"
  (let [shape (types/make-jsonb-shape {"id" :uuid})
        ctx (types/set-var-shape (types/make-context) 'user shape)]
    (types/get-var-shape ctx 'user) => shape)
  (let [shape (types/make-jsonb-shape {"id" :uuid})
        parent (types/set-var-shape (types/make-context) 'user shape)
        child (types/push-scope parent)]
    (types/get-var-shape child 'user) => shape))

^{:refer rt.postgres.grammar.typed-common/set-var-shape :added "0.1"}
(fact "set-var-shape sets shape for variable"
  (let [shape (types/make-jsonb-shape {"id" :uuid})
        ctx (types/set-var-shape (types/make-context) 'user shape)]
    (get-in ctx [:jsonb-shapes 'user]) => shape))

^{:refer rt.postgres.grammar.typed-common/normalize-key :added "0.1"}
(fact "normalize-key converts keys to consistent format"
  (types/normalize-key :id) => "id"
  (types/normalize-key "name") => "name"
  (types/normalize-key 'handle) => "handle")


^{:refer rt.postgres.grammar.typed-common/add-typed :added "4.1"}
(fact "add-typed stores table, enum, and function defs by stable key"
  (let [table (types/make-table-def "demo" "User" [] :id)
        enum (types/make-enum-def "demo" "Status" #{:active} nil)
        fn-def (types/make-fn-def "demo" "get-user" [] [:jsonb] {} nil)]
    (keys (:tables (types/add-typed (types/empty-typed) table))) => ['demo/User]
    (keys (:enums (types/add-typed (types/empty-typed) enum))) => ['demo/Status]
    (keys (:functions (types/add-typed (types/empty-typed) fn-def))) => ['demo/get-user]))
