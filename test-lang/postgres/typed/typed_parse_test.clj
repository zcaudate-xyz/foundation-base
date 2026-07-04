(ns postgres.typed.typed-parse-test
  (:require [postgres.typed.typed-common :as types]
            [postgres.typed.typed-parse :as parse])
  (:use code.test))

^{:refer postgres.typed.typed-parse/read-forms :added "0.1"}
(fact "read-forms reads top-level forms from a file"
  (let [forms (parse/read-forms "src-lang/postgres/sample/scratch_v2.clj")]
    (vector? forms) => true))

^{:refer postgres.typed.typed-parse/deftype? :added "0.1"}
(fact "deftype? identifies deftype.pg forms"
  (parse/deftype? '(deftype.pg User [:id {:type :uuid}])) => true
  (parse/deftype? '(deftype User [:id {:type :uuid}])) => false
  (parse/deftype? '(defn.pg test [])) => false
  (parse/deftype? "not a form") => false
  (parse/deftype? nil) => false)

^{:refer postgres.typed.typed-parse/defenum? :added "0.1"}
(fact "defenum? identifies defenum.pg forms"
  (parse/defenum? '(defenum.pg Status [:active :inactive])) => true
  (parse/defenum? '(defenum Status [:active])) => false
  (parse/defenum? '(deftype.pg User [])) => false
  (parse/defenum? nil) => false)

^{:refer postgres.typed.typed-parse/defn? :added "0.1"}
(fact "defn? identifies defn.pg forms"
  (parse/defn? '(defn.pg test [:uuid id] (return id))) => true
  (parse/defn? '(defn test [])) => false
  (parse/defn? '(deftype.pg User [])) => false
  (parse/defn? nil) => false)

^{:refer postgres.typed.typed-parse/script? :added "4.1"}
(fact "script? identifies postgres script forms"
  (parse/script? '(script :postgres {:require []})) => true
  (parse/script? '(script :mysql {:require []})) => false
  (parse/script? '(defn test [])) => false)

^{:refer postgres.typed.typed-parse/parse-schema :added "4.1"}
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

^{:refer postgres.typed.typed-parse/extract-aliases :added "4.1"}
(fact "extract-aliases builds alias map from require forms"
  ;; Single alias
  (parse/extract-aliases '[[my.ns :as m]])
  => '{m my.ns}

  ;; Multiple aliases
  (parse/extract-aliases '[[postgres.core :as pg] [hara.runtime.user :as user]])
  => '{pg postgres.core, user hara.runtime.user}

  ;; Ignores non-:as forms
  (parse/extract-aliases '[[postgres.core :as pg] [hara.runtime.util :refer [foo]]])
  => '{pg postgres.core}

  ;; Empty input
  (parse/extract-aliases []) => {})

^{:refer postgres.typed.typed-parse/parse-aliases :added "4.1"}
(fact "parse-aliases extracts aliases from script form"
  ;; From script with :require
  (parse/parse-aliases '(script :postgres {:require [[postgres.core :as pg]
                                                     [hara.runtime.user :as u]]}))
  => '{pg postgres.core, u hara.runtime.user}

  ;; Empty require
  (parse/parse-aliases '(script :postgres {:require []}))
  => {}

  ;; No script form
  (parse/parse-aliases '(other-form)) => nil)

^{:refer postgres.typed.typed-parse/parse-process-constraints :added "4.1"}
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

^{:refer postgres.typed.typed-parse/parse-column-spec :added "0.1"}
(fact "parse-column-spec handles map schemas"
  (let [col (parse/parse-column-spec [:settings {:type :map :map {:theme {:type :text}}}])]
    (:name col) => :settings
    (:map-schema col) => {:theme {:type :text}}))

^{:refer postgres.typed.typed-parse/parse-deftype :added "0.1"}
(fact "parse-deftype handles addons"
  (let [form '(deftype.pg ^{:! (et/E {:addons [:feed]})} Organisation
                [:id {:type :uuid}])
        table (parse/parse-deftype form "test.ns" nil)]
    (:name table) => "Organisation"
    (:addons table) => [:feed]))

^{:refer postgres.typed.typed-parse/parse-defenum :added "0.1"}
(fact "parse-defenum handles docstrings"
  (let [form '(defenum.pg Priority "Priority levels" [:low :medium :high])
        enum (parse/parse-defenum form "test.ns" nil)]
    (:name enum) => "Priority"
    (set (:values enum)) => #{:low :medium :high}))

^{:refer postgres.typed.typed-parse/parse-fn-inputs :added "0.1"}
(fact "parse-fn-inputs extracts function arguments"
  (parse/parse-fn-inputs [:uuid 'i-id])
  => (contains [(contains {:name 'i-id :type :uuid :role :payload})])
  (parse/parse-fn-inputs [:uuid 'i-id :text 'i-handle])
  => (contains [(contains {:name 'i-id :type :uuid :role :payload})
                (contains {:name 'i-handle :type :text :role :payload})])
  (parse/parse-fn-inputs [:jsonb 'o-op])
  => (contains [(contains {:name 'o-op :type :jsonb :role :payload})]))

^{:refer postgres.typed.typed-parse/infer-fn-arg-role :added "4.1"}
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

^{:refer postgres.typed.typed-parse/parse-defn :added "0.1"}
(fact "parse-defn extracts return type from :- metadata"
  (let [form '(defn.pg ^{:%% :sql :- Entry}
                insert-entry
                "inserts an entry"
                [:text i-name :jsonb i-tags]
                (pg/t:insert Entry {:name i-name :tags i-tags}))
        fn-def (parse/parse-defn form "test.ns" nil)]
    ;; The output field contains the return type
    (:output fn-def) => 'Entry))

^{:refer postgres.typed.typed-parse/transform-col-opts :added "4.1"}
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

^{:refer postgres.typed.typed-parse/parse-runtime-table :added "4.1"}
(fact "parse-runtime-table handles ref link transformation"
  (let [entries [:id {:type :uuid :primary true}
                 :org-id {:type :ref :ref {:link {:module :Organisation :id :id}}}]
        table (parse/parse-runtime-table :User entries "gwdb.core")]
    (:name table) => "User"
    (count (:columns table)) => 2))

^{:refer postgres.typed.typed-parse/analyze-tables :added "4.1"}
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

^{:refer postgres.typed.typed-parse/analyze-file :added "0.1"}
(fact "analyze-file returns structure with all type definitions"
  (let [result (parse/analyze-file "src-lang/postgres/sample/scratch_v2.clj")]
    (contains? result :tables) => true
    (contains? result :enums) => true
    (contains? result :functions) => true))

^{:refer postgres.typed.typed-parse/register-types! :added "0.1"}
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

^{:refer postgres.typed.typed-parse/analyze-namespace :added "0.1"}
(fact "analyze-namespace analyzes a namespace"
  (let [result (parse/analyze-namespace 'postgres.sample.scratch-v2)]
    (contains? result :tables) => true
    (contains? result :enums) => true
    (contains? result :functions) => true))
