(ns rt.postgres.grammar.typed-parse-test
  (:require [rt.postgres.grammar.typed-common :as types]
            [rt.postgres.grammar.typed-parse :as parse])
  (:use code.test))

;; -----------------------------------------------------------------------------
;; Form Identification Tests
;; -----------------------------------------------------------------------------

^{:refer rt.postgres.grammar.typed-parse/deftype? :added "0.1"}
(fact "deftype? identifies deftype.pg forms"
  (parse/deftype? '(deftype.pg User [:id {:type :uuid}])) => true
  (parse/deftype? '(deftype User [:id {:type :uuid}])) => false
  (parse/deftype? '(defn.pg test [])) => false
  (parse/deftype? "not a form") => false
  (parse/deftype? nil) => false)

^{:refer rt.postgres.grammar.typed-parse/defenum? :added "0.1"}
(fact "defenum? identifies defenum.pg forms"
  (parse/defenum? '(defenum.pg Status [:active :inactive])) => true
  (parse/defenum? '(defenum Status [:active])) => false
  (parse/defenum? '(deftype.pg User [])) => false
  (parse/defenum? nil) => false)

^{:refer rt.postgres.grammar.typed-parse/defn? :added "0.1"}
(fact "defn? identifies defn.pg forms"
  (parse/defn? '(defn.pg test [:uuid id] (return id))) => true
  (parse/defn? '(defn test [])) => false
  (parse/defn? '(deftype.pg User [])) => false
  (parse/defn? nil) => false)

;; -----------------------------------------------------------------------------
;; Column Parsing Tests
;; -----------------------------------------------------------------------------

^{:refer rt.postgres.grammar.typed-parse/parse-column-spec :added "0.1"}
(fact "parse-column-spec parses basic column definitions"
  (let [col (parse/parse-column-spec [:id {:type :uuid :primary true}])]
    (:name col) => :id
    (= :primitive (:kind (:type col))) => true)
  (let [col (parse/parse-column-spec [:handle {:type :citext :required true}])]
    (:name col) => :handle
    (:required col) => true)
  (let [col (parse/parse-column-spec [:email {:type :text :unique true}])]
    (:name col) => :email
    (get-in col [:constraints :unique]) => true))

^{:refer rt.postgres.grammar.typed-parse/parse-column-spec :added "0.1"}
(fact "parse-column-spec handles enum types"
  (let [col (parse/parse-column-spec [:status {:type :enum :enum {:ns :test}}])]
    (:name col) => :status
    (types/enum? (:type col)) => true))

^{:refer rt.postgres.grammar.typed-parse/parse-column-spec :added "0.1"}
(fact "parse-column-spec handles ref types"
  (let [col (parse/parse-column-spec [:user-id {:type :ref :ref {:ns :user}}])]
    (:name col) => :user-id
    (types/ref? (:type col)) => true))

^{:refer rt.postgres.grammar.typed-parse/parse-column-spec :added "0.1"}
(fact "parse-column-spec handles map schemas"
  (let [col (parse/parse-column-spec [:settings {:type :map :map {:theme {:type :text}}}])]
    (:name col) => :settings
    (:map-schema col) => {:theme {:type :text}}))

;; -----------------------------------------------------------------------------
;; deftype.pg Parsing Tests
;; -----------------------------------------------------------------------------

^{:refer rt.postgres.grammar.typed-parse/parse-deftype :added "0.1"}
(fact "parse-deftype extracts table definition"
  (let [form '(deftype.pg User
                [:id {:type :uuid :primary true}
                 :handle {:type :citext :required true}])
        table (parse/parse-deftype form "test.ns" nil)]
    (:name table) => "User"
    (:ns table) => "test.ns"
    (count (:columns table)) => 2
    (:primary-key table) => :id))

^{:refer rt.postgres.grammar.typed-parse/parse-deftype :added "0.1"}
(fact "parse-deftype handles docstrings and metadata"
  (let [form '(deftype.pg ^{:! (et/E {})} User "A user"
                {:added "0.1"}
                [:id {:type :uuid}])
        table (parse/parse-deftype form "test.ns" nil)]
    (:name table) => "User"
    (count (:columns table)) => 1))

^{:refer rt.postgres.grammar.typed-parse/parse-deftype :added "0.1"}
(fact "parse-deftype handles addons"
  (let [form '(deftype.pg ^{:! (et/E {:addons [:feed]})} Organisation
                [:id {:type :uuid}])
        table (parse/parse-deftype form "test.ns" nil)]
    (:name table) => "Organisation"
    (:addons table) => [:feed]))

;; -----------------------------------------------------------------------------
;; defenum.pg Parsing Tests
;; -----------------------------------------------------------------------------

^{:refer rt.postgres.grammar.typed-parse/parse-defenum :added "0.1"}
(fact "parse-defenum extracts enum definition"
  (let [form '(defenum.pg Status [:active :inactive :pending])
        enum (parse/parse-defenum form "test.ns" nil)]
    (:name enum) => "Status"
    (:ns enum) => "test.ns"
    (set (:values enum)) => #{:active :inactive :pending}))

^{:refer rt.postgres.grammar.typed-parse/parse-defenum :added "0.1"}
(fact "parse-defenum handles docstrings"
  (let [form '(defenum.pg Priority "Priority levels" [:low :medium :high])
        enum (parse/parse-defenum form "test.ns" nil)]
    (:name enum) => "Priority"
    (set (:values enum)) => #{:low :medium :high}))

;; -----------------------------------------------------------------------------
;; defn.pg Parsing Tests
;; -----------------------------------------------------------------------------

^{:refer rt.postgres.grammar.typed-parse/parse-fn-inputs :added "0.1"}
(fact "parse-fn-inputs extracts function arguments"
  (parse/parse-fn-inputs [:uuid i-id])
  => (contains [(contains {:name 'i-id :type :uuid :role :payload})])
  (parse/parse-fn-inputs [:uuid i-id :text i-handle])
  => (contains [(contains {:name 'i-id :type :uuid :role :payload})
                (contains {:name 'i-handle :type :text :role :payload})])
  (parse/parse-fn-inputs [:jsonb o-op])
  => (contains [(contains {:name 'o-op :type :jsonb :role :payload})]))

^{:refer rt.postgres.grammar.typed-parse/parse-defn :added "0.1"}
(fact "parse-defn marks track arguments when they flow into :track"
  (let [form '(defn.pg test-fn
                [:jsonb o-op]
                (pg/t:update Task {:set {:x 1}
                                    :track o-op}))
        fn-def (parse/parse-defn form "test.ns" nil)]
    (:name fn-def) => "test-fn"
    (:role (first (:inputs fn-def))) => :track))

^{:refer rt.postgres.grammar.typed-parse/parse-defn :added "0.1"}
(fact "parse-defn extracts function definition"
  (let [form '(defn.pg test-fn [:uuid i-id] (return i-id))
        fn-def (parse/parse-defn form "test.ns" nil)]
    (:name fn-def) => "test-fn"
    (:ns fn-def) => "test.ns"
    (count (:inputs fn-def)) => 1))

^{:refer rt.postgres.grammar.typed-parse/parse-defn :added "0.1"}
(fact "parse-defn handles metadata"
  (let [form '(defn.pg ^{:%% :sql :- [:jsonb]} test-fn [:uuid i-id] (return {}))
        fn-def (parse/parse-defn form "test.ns" nil)]
    (:name fn-def) => "test-fn"
    (get-in fn-def [:body-meta :lang]) => :sql))

^{:refer rt.postgres.grammar.typed-parse/parse-defn :added "0.1"}
(fact "parse-defn handles docstrings"
  (let [form '(defn.pg test-fn "Test function" [:uuid i-id] (return i-id))
        fn-def (parse/parse-defn form "test.ns" nil)]
    (:name fn-def) => "test-fn"
    (get-in fn-def [:body-meta :docstring]) => "Test function"))

^{:refer rt.postgres.grammar.typed-parse/parse-defn :added "0.1"}
(fact "parse-defn extracts return type from :- metadata"
  (let [form '(defn.pg ^{:%% :sql :- Entry}
                insert-entry
                "inserts an entry"
                [:text i-name :jsonb i-tags]
                (pg/t:insert Entry {:name i-name :tags i-tags}))
        fn-def (parse/parse-defn form "test.ns" nil)]
    ;; The output field contains the return type
    (:output fn-def) => 'Entry))

;; -----------------------------------------------------------------------------
;; File Analysis Tests
;; -----------------------------------------------------------------------------

^{:refer rt.postgres.grammar.typed-parse/read-forms :added "0.1"}
(fact "read-forms reads top-level forms from a file"
  (let [forms (parse/read-forms "src/rt/postgres/grammar/typed_common.clj")]
    (vector? forms) => true))

^{:refer rt.postgres.grammar.typed-parse/analyze-file :added "0.1"}
(fact "analyze-file returns structure with all type definitions"
  (let [result (parse/analyze-file "src/rt/postgres/grammar/typed_common.clj")]
    (contains? result :tables) => true
    (contains? result :enums) => true
    (contains? result :functions) => true))

^{:refer rt.postgres.grammar.typed-parse/analyze-namespace :added "0.1"}
(fact "analyze-namespace analyzes a namespace"
  (let [result (parse/analyze-namespace 'rt.postgres.grammar.typed-common)]
    (contains? result :tables) => true
    (contains? result :enums) => true
    (contains? result :functions) => true))

;; -----------------------------------------------------------------------------
;; Registry Population Tests
;; -----------------------------------------------------------------------------

^{:refer rt.postgres.grammar.typed-parse/register-types! :added "0.1"}
(fact "register-types! adds types to registry"
  (types/clear-registry!)
  (let [analysis {:tables [(types/make-table-def "ns" "TestTable" [] :id)]
                  :enums [(types/make-enum-def "ns" "TestEnum" #{:a :b} nil)]
                  :functions [(types/make-fn-def "ns" "testFn" [] [:jsonb] {} nil)]}]
    (parse/register-types! analysis)
    (some? (types/get-type (symbol "ns" "TestTable"))) => true
    (some? (types/get-type (symbol "ns" "TestEnum"))) => true
    (some? (types/get-type (symbol "ns" "testFn"))) => true)
  (types/clear-registry!))

;; -----------------------------------------------------------------------------
;; App Table Parsing Tests
;; -----------------------------------------------------------------------------

^{:refer rt.postgres.grammar.typed-parse/parse-runtime-table :added "4.1"}
(fact "parse-runtime-table parses a common-application table entry vector"
  (let [entries [:id {:type :uuid :primary true}
                 :name {:type :text}
                 :email {:type :citext}]
        table (parse/parse-runtime-table :User entries "gwdb.core")]
    (:name table) => "User"
    (:ns table) => "gwdb.core"
    (count (:columns table)) => 3
    (:primary-key table) => :id))

^{:refer rt.postgres.grammar.typed-parse/parse-runtime-table :added "4.1"}
(fact "parse-runtime-table handles ref link transformation"
  (let [entries [:id {:type :uuid :primary true}
                 :org-id {:type :ref :ref {:link {:module :Organisation :id :id}}}]
        table (parse/parse-runtime-table :User entries "gwdb.core")]
    (:name table) => "User"
    (count (:columns table)) => 2))

^{:refer rt.postgres.grammar.typed-parse/analyze-tables :added "4.1"}
(fact "analyze-tables converts app tables into typed table defs"
  (let [analysis (parse/analyze-tables {:User [:id {:type :uuid :primary true}
                                               :name {:type :text}]
                                        :Organisation [:id {:type :uuid :primary true}
                                                       :handle {:type :citext}]}
                                       "gwdb.core")]
    (count (:tables analysis)) => 2
    (count (:enums analysis)) => 0
    (count (:functions analysis)) => 0
    (map :name (:tables analysis)) => ["User" "Organisation"]))


^{:refer rt.postgres.grammar.typed-parse/script? :added "4.1"}
(fact "script? identifies postgres script forms"
  (parse/script? '(script :postgres {:require []})) => true
  (parse/script? '(script :mysql {:require []})) => false
  (parse/script? '(defn test [])) => false)


^{:refer rt.postgres.grammar.typed-parse/parse-schema :added "4.1"}
(fact "parse-schema extracts schema name from script form"
  ;; From :static :all :schema
  (parse/parse-schema '(script :postgres {:static {:all {:schema ["gw_data"]}}}))
  => "gw_data"

  ;; From :static :seed :schema
  (parse/parse-schema '(script :postgres {:static {:seed {:schema ["gw_seed"]}}}))
  => "gw_seed"

  ;; :all takes precedence
  (parse/parse-schema '(script :postgres {:static {:all {:schema ["all_schema"]}
                                                   :seed {:schema ["seed_schema"]}}}))
  => "all_schema"

  ;; Nil when no schema
  (parse/parse-schema '(script :postgres {})) => nil
  (parse/parse-schema '(other-form)) => nil)

^{:refer rt.postgres.grammar.typed-parse/extract-aliases :added "4.1"}
(fact "extract-aliases builds alias map from require forms"
  ;; Single alias
  (parse/extract-aliases '[[my.ns :as m]])
  => '{m my.ns}

  ;; Multiple aliases
  (parse/extract-aliases '[[rt.postgres :as pg] [rt.user :as user]])
  => '{pg rt.postgres, user rt.user}

  ;; Ignores non-:as forms
  (parse/extract-aliases '[[rt.postgres :as pg] [rt.util :refer [foo]]])
  => '{pg rt.postgres}

  ;; Empty input
  (parse/extract-aliases []) => {})

^{:refer rt.postgres.grammar.typed-parse/parse-aliases :added "4.1"}
(fact "parse-aliases extracts aliases from script form"
  ;; From script with :require
  (parse/parse-aliases '(script :postgres {:require [[rt.postgres :as pg]
                                                     [rt.user :as u]]}))
  => '{pg rt.postgres, u rt.user}

  ;; Empty require
  (parse/parse-aliases '(script :postgres {:require []}))
  => {}

  ;; No script form
  (parse/parse-aliases '(other-form)) => nil)

^{:refer rt.postgres.grammar.typed-parse/parse-process-constraints :added "4.1"}
(fact "parse-process-constraints extracts constraints from SQL process"
  ;; as-limit-length
  (parse/parse-process-constraints '((as-limit-length 255)))
  => {:max-length 255}

  ;; as-lower-formatted
  (parse/parse-process-constraints '((as-lower-formatted)))
  => {:format :handle}

  ;; as-email
  (parse/parse-process-constraints '((as-email)))
  => {:format :email}

  ;; as-url
  (parse/parse-process-constraints '((as-url)))
  => {:format :uri}

  ;; Combined
  (parse/parse-process-constraints '((as-limit-length 100) (as-email)))
  => {:max-length 100, :format :email}

  ;; Empty/nil
  (parse/parse-process-constraints nil) => {}
  (parse/parse-process-constraints []) => {})

^{:refer rt.postgres.grammar.typed-parse/transform-col-opts :added "4.1"}
(fact "transform-col-opts transforms runtime column options"
  ;; Transforms ref link
  (parse/transform-col-opts {:type :ref :ref {:link {:module :Organisation :id :id}}})
  => {:type :ref, :ref {:key :Organisation/id}}

  ;; Passes through non-ref opts
  (parse/transform-col-opts {:type :uuid :required true})
  => {:type :uuid :required true}

  ;; Passes through opts without link
  (parse/transform-col-opts {:type :ref :ref {:ns :test}})
  => {:type :ref :ref {:ns :test}})


^{:refer rt.postgres.grammar.typed-parse/infer-fn-arg-role :added "4.1"}
(fact "infer-fn-arg-role marks args used in :track positions"
  (parse/infer-fn-arg-role
   'o-op
   '((pg/t:update -/Task {:set {:status o-op}
                          :track o-op})))
  => :track

  (parse/infer-fn-arg-role
   'x
   '((return x)))
  => :payload)
