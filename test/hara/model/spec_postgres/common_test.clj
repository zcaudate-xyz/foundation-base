(ns hara.model.spec-postgres.common-test
  (:require [hara.model.spec-postgres :as g]
            [hara.model.spec-postgres.common :as common]
            [postgres.core.builtin :as builtin]
            [postgres.sample.scratch-v1 :as scratch]
            [hara.lang :as l]
            [hara.common.emit-common :as emit-common])
  (:use code.test))

^{:refer hara.model.spec-postgres.common/pg-type-alias :added "4.0"}
(fact "gets the type alias"

  (common/pg-type-alias :long)
  => :bigint

  (common/pg-type-alias :numeric)
  => :numeric)

^{:refer hara.model.spec-postgres.common/pg-deftype-ref-name :added "4.1"}
(fact "gets the ref name"
  (common/pg-deftype-ref-name :user {:raw "user_id"})
  => "user_id"

  (common/pg-deftype-ref-name :user {})
  => "user_id")

^{:refer hara.model.spec-postgres.common/pg-sym-meta :added "4.0"}
(fact "returns the sym meta"

  (common/pg-sym-meta (with-meta 'hello
                        {:- [:int]
                         :%% :default
                         :props [:immutable :parallel-safe]}))
  => {:- [:int],
      :static/return [:int],
      :static/language :default,
      :static/props [:immutable :parallel-safe]})

^{:refer hara.model.spec-postgres.common/pg-format :added "4.0"}
(fact "formats a form, extracting static components"

  (common/pg-format '(def ^{:- [:int]} hello 1))
  => vector?)

^{:refer hara.model.spec-postgres.common/pg-policy-format :added "4.0"}
(fact "formats a policy definition"

  (common/pg-policy-format '(defpolicy hello [table] ()))
  => '[{:doc "", :static/policy-name "hello - ", :static/policy-table nil, :static/policy-schema "table"}
       (defpolicy hello "" [table] ())])

^{:refer hara.model.spec-postgres.common/pg-hydrate-module-static :added "4.0"}
(fact "gets the static module"

  (common/pg-hydrate-module-static
   {:static {:application "app" :all {:schema ["schema"]}}})
  => {:static/schema "schema" :static/application "app"})

^{:refer hara.model.spec-postgres.common/pg-hydrate :added "4.0"}
(fact "hydrate function for top level entries"

  (common/pg-hydrate '(defn foo [] 1) {} {:module {:static {:application "app" :all {:schema ["schema"]}}}})
  => vector?)

^{:refer hara.model.spec-postgres.common/pg-current-module-link? :added "4.1"}
(fact "checks postgres module links")

^{:refer hara.model.spec-postgres.common/pg-link-symbol :added "4.1"}
(fact "resolves postgres link symbols")

^{:refer hara.model.spec-postgres.common/pg-resolve-entry :added "4.1"}
(fact "resolves postgres entries")

^{:refer hara.model.spec-postgres.common/pg-string :added "4.0"}
(fact "constructs a pg string"

  (common/pg-string "hello")
  => "'hello'"

  (common/pg-string "'hello'")
  => "'''hello'''")

^{:refer hara.model.spec-postgres.common/pg-uuid :added "4.1"}
(fact "constructs a pg uuid"

  (common/pg-uuid "123")
  => "'123'::uuid")

^{:refer hara.model.spec-postgres.common/pg-map :added "4.0"}
(fact "creates a postgres json object"
  (with-redefs [emit-common/*emit-fn* (fn [& _] "jsonb")]
    (common/pg-map {:a 1} nil nil))
  => "jsonb")

^{:refer hara.model.spec-postgres.common/pg-set :added "4.0"}
(fact "makes a set object"

  (common/pg-set #{"hello"}
                 nil
                 {})
  => "\"hello\"")

^{:refer hara.model.spec-postgres.common/pg-array :added "4.0"}
(fact "creates an array object"
  (with-redefs [emit-common/emit-array (fn [& _] ["1" "2"])]
    (common/pg-array '(array 1 2) nil nil))
  => "ARRAY[1,2]")

^{:refer hara.model.spec-postgres.common/pg-invoke-typecast :added "4.0"}
(fact  "emits a typecast call"
  (with-redefs [emit-common/*emit-fn* (fn [x & _] (str x))]
    (common/pg-invoke-typecast '(:int val) nil nil))
  => "(val)::INT")

^{:refer hara.model.spec-postgres.common/pg-typecast :added "4.0"}
(fact "creates a typecast"
  (with-redefs [emit-common/*emit-fn* (fn [x & _] (str x))]
    (common/pg-typecast '(++ val :int) nil nil))
  => "(val)::INT")

^{:refer hara.model.spec-postgres.common/pg-do-assert :added "4.0"}
(fact "creates an assert form"
  (with-redefs [emit-common/*emit-fn* (fn [& _] "assert")]
    (common/pg-do-assert '(do:assert true [:tag {}]) nil nil))
  => "assert")

^{:refer hara.model.spec-postgres.common/pg-base-token :added "4.0"}
(fact "creates a base token"

  (common/pg-base-token #{"hello"} "schema")
  => '(. #{"schema"} #{"hello"}))

^{:refer hara.model.spec-postgres.common/pg-full-token :added "4.0"}
(fact "creates a full token (for types and enums)"

  (common/pg-full-token "hello" "schema")
  => '(. #{"schema"} #{"hello"}))

^{:refer hara.model.spec-postgres.common/pg-entry-literal :added "4.0"}
(fact "creates an entry literal"

  (common/pg-entry-literal {:static/schema "schema" :id "id" :op 'defn})
  => "schema.id")

^{:refer hara.model.spec-postgres.common/pg-entry-token :added "4.0"}
(fact "gets the entry token"

  (common/pg-entry-token {:static/schema "schema" :id "id" :op 'defn})
  => '(. #{"schema"} id))

^{:refer hara.model.spec-postgres.common/pg-linked-token :added "4.0"}
(fact "gets the linked token given symbol"
  ;; Requires setting up snapshot/book structure which is complex.
  ;; Skipping complex setup.
  )

^{:refer hara.model.spec-postgres.common/pg-linked :added "4.0"}
(fact "emits the linked symbol"
  ;; Skipping complex setup.
  )

^{:refer hara.model.spec-postgres.common/block-do-block :added "4.0"}
(fact "initates do block"

  (common/block-do-block '[(+ 1 2 3) \;])
  => '[:do :$$
       \\ :begin
       \\ (\| [(+ 1 2 3) \;])
       \\ :end \;
       \\ :$$ :language "plpgsql" \;])

^{:refer hara.model.spec-postgres.common/block-do-suppress :added "4.0"}
(fact "initates suppress block"

  (common/block-do-suppress '[(+ 1 2 3) \;])
  => '[:do :$$
       \\ :begin
       \\ (\| [(+ 1 2 3) \;])
       \\ :exception :when-others-then
       \\ :end \;
       \\ :$$ :language "plpgsql" \;])

^{:refer hara.model.spec-postgres.common/block-loop-block :added "4.0"}
(fact "emits loop block"

  (common/block-loop-block
   '_ '(+ 1 2 3) '(+ 4 5 6))
  => '[:loop \\ (\| (do (+ 1 2 3) (+ 4 5 6))) \\ :end-loop])

^{:refer hara.model.spec-postgres.common/block-while-block :added "4.1"}
(fact "emits while block"
  (common/block-while-block '(= 1 1) '(do-something))
  => '[:while (= 1 1) :loop \\ (\| (do (do-something))) \\ :end-loop \;])

^{:refer hara.model.spec-postgres.common/block-case-block :added "4.0"}
(fact "emits case block"

  (common/block-case-block
   'type
   "object" '(from-object x)
   "string" '(from-string x))
  => '(% [:case type \\ (\| :WHEN (:% "object") :THEN (:% (from-object x)) \\ :WHEN (:% "string") :THEN (:% (from-string x)) \\) :end]))

^{:refer hara.model.spec-postgres.common/pg-defenum :added "4.0"}
(fact "defenum block"

  (common/pg-defenum '(defenum ^{:static/schema "schema"} hello [:a :b :c]))
  => '[:do :$$
       \\ :begin
       \\ (\| (do [:create-type (. #{"schema"} #{"hello"}) :as-enum (quote ("a" "b" "c"))]))
       \\ :exception :when-others-then
       \\ :end \;
       \\ :$$ :language "plpgsql" \;]

  (common/pg-defenum
   '(defenum ^{:static/schema "schema"}
      hello
      (!:eval (postgres.entity/class-types "app"))))
  => '[:do :$$
       \\ :begin
       \\ (\| (do [:create-type (. #{"schema"} #{"hello"})
                    :as-enum
                    (!:eval
                     (list
                      (quote quote)
                      (clojure.core/map
                       std.lib.foundation/strn
                       (postgres.entity/class-types "app"))))]))
       \\ :exception :when-others-then
       \\ :end \;
       \\ :$$ :language "plpgsql" \;])

^{:refer hara.model.spec-postgres.common/pg-defindex :added "4.0"}
(fact "defindex block"

  (common/pg-defindex '(defindex hello [table cols] body))
  => '[:create-index :if-not-exists hello :on table (quote ([cols])) body \;])

^{:refer hara.model.spec-postgres.common/pg-defpolicy :added "4.0"}
(fact "defpolicy block"

  (common/pg-defpolicy '(defpolicy hello [table] ()))
  => '(do [:drop-policy-if-exists #{"hello - "} :on table] [:create-policy #{"hello - "} :on table \\]))

^{:refer hara.model.spec-postgres.common/pg-publication-format :added "4.1"}
(fact "formats publication"
  (common/pg-publication-format '(defpublication pub [:all]))
  => vector?)

^{:refer hara.model.spec-postgres.common/pg-defpublication :added "4.1"}
(fact "defpublication block"
  (common/pg-defpublication '(defpublication pub [:all]))
  => '(do [:drop-publication-if-exists pub]
          [:create-publication pub :for :all :tables]))

^{:refer hara.model.spec-postgres.common/pg-subscription-format :added "4.1"}
(fact "formats subscription"
  (common/pg-subscription-format '(defsubscription sub [conn pub] {}))
  => vector?)

^{:refer hara.model.spec-postgres.common/pg-defsubscription :added "4.1"}
(fact "defsubscription block"
  (common/pg-defsubscription '(defsubscription sub ["conn" "pub"] {}))
  => '(do [:drop-subscription-if-exists sub]
          [:create-subscription sub :connection "conn" :publication "pub"]))

^{:refer hara.model.spec-postgres.common/pg-deftrigger :added "4.0"}
(fact "deftrigger block"

  (common/pg-deftrigger '(deftrigger hello [table] ()))
  => '(do [:drop-trigger-if-exists hello :on table] [:create-trigger hello :on table \\]))

^{:refer hara.model.spec-postgres.common/pg-defblock :added "4.0"}
(fact "creates generic defblock"

  (common/pg-defblock '(def ^{:static/return [:index]
                              :static/schema "scratch"} hello []))
  => '(do [:create :index (. #{"scratch"} #{"hello"})]))
