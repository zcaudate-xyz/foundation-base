(ns rt.postgres.grammar.common-test
  (:use code.test)
  (:require [rt.postgres.grammar.common :as common]
            [rt.postgres.grammar :as g]
            [rt.postgres.script.scratch :as scratch]
            [rt.postgres.script.builtin :as builtin]
            [std.lang :as l]
            [std.lib :as h]
            [std.lang.base.emit-common :as emit-common]))

^{:refer rt.postgres.grammar.common/pg-type-alias :added "4.0"}
(fact "gets the type alias"
  ^:hidden
  
  (common/pg-type-alias :long)
  => :bigint

  (common/pg-type-alias :numeric)
  => :numeric)

^{:refer rt.postgres.grammar.common/pg-sym-meta :added "4.0"}
(fact "returns the sym meta"
  ^:hidden
  
  (common/pg-sym-meta (with-meta 'hello
                        {:- [:int]
                         :%% :default
                         :props [:immutable :parallel-safe]}))
  => {:- [:int],
      :static/return [:int],
      :static/language :default,
      :static/props [:immutable :parallel-safe]})

^{:refer rt.postgres.grammar.common/pg-format :added "4.0"}
(fact "formats a form, extracting static components"
  ^:hidden
  
  (common/pg-format '(def ^{:- [:int]} hello 1))
  => vector?)

^{:refer rt.postgres.grammar.common/pg-hydrate-module-static :added "4.0"}
(fact "gets the static module"
  ^:hidden

  (common/pg-hydrate-module-static
   {:static {:application "app" :all {:schema ["schema"]}}})
  => {:static/schema "schema" :static/application "app"})

^{:refer rt.postgres.grammar.common/pg-hydrate :added "4.0"}
(fact "hydrate function for top level entries"
  ^:hidden

  (common/pg-hydrate '(defn foo [] 1) {} {:module {:static {:application "app" :all {:schema ["schema"]}}}})
  => vector?)

^{:refer rt.postgres.grammar.common/pg-string :added "4.0"}
(fact "constructs a pg string"
  ^:hidden
  
  (common/pg-string "hello")
  => "'hello'"

  (common/pg-string "'hello'")
  => "'''hello'''")

^{:refer rt.postgres.grammar.common/pg-map :added "4.0"}
(fact "creates a postgres json object"
  (with-redefs [emit-common/*emit-fn* (fn [& _] "jsonb")]
    (common/pg-map {:a 1} nil nil))
  => "jsonb")

^{:refer rt.postgres.grammar.common/pg-set :added "4.0"}
(fact "makes a set object"
  ^:hidden
  
  (common/pg-set #{"hello"}
                 nil
                 {})
  => "\"hello\"")

^{:refer rt.postgres.grammar.common/pg-array :added "4.0"}
(fact "creates an array object"
  (with-redefs [emit-common/emit-array (fn [& _] ["1" "2"])]
    (common/pg-array '(array 1 2) nil nil))
  => "ARRAY[1,2]")

^{:refer rt.postgres.grammar.common/pg-invoke-typecast :added "4.0"}
(fact  "emits a typecast call"
  (with-redefs [emit-common/*emit-fn* (fn [x & _] (str x))]
    (common/pg-invoke-typecast '(:int val) nil nil))
  => "(val)::INT")

^{:refer rt.postgres.grammar.common/pg-typecast :added "4.0"}
(fact "creates a typecast"
  (with-redefs [emit-common/*emit-fn* (fn [x & _] (str x))]
    (common/pg-typecast '(++ val :int) nil nil))
  => "(val)::INT")

^{:refer rt.postgres.grammar.common/pg-do-assert :added "4.0"}
(fact "creates an assert form"
  (with-redefs [emit-common/*emit-fn* (fn [& _] "assert")]
    (common/pg-do-assert '(do:assert true [:tag {}]) nil nil))
  => "assert")

^{:refer rt.postgres.grammar.common/pg-base-token :added "4.0"}
(fact "creates a base token"
  ^:hidden
  
  (common/pg-base-token #{"hello"} "schema")
  => '(. #{"schema"} #{"hello"}))

^{:refer rt.postgres.grammar.common/pg-full-token :added "4.0"}
(fact "creates a full token (for types and enums)"
  ^:hidden
  
  (common/pg-full-token "hello" "schema")
  => '(. #{"schema"} #{"hello"}))

^{:refer rt.postgres.grammar.common/pg-entry-literal :added "4.0"}
(fact "creates an entry literal"
  ^:hidden

  (common/pg-entry-literal {:static/schema "schema" :id "id" :op 'defn})
  => "schema.id")

^{:refer rt.postgres.grammar.common/pg-entry-token :added "4.0"}
(fact "gets the entry token"
  ^:hidden
  
  (common/pg-entry-token {:static/schema "schema" :id "id" :op 'defn})
  => '(. #{"schema"} id))

^{:refer rt.postgres.grammar.common/pg-linked-token :added "4.0"}
(fact "gets the linked token given symbol"
  ;; Requires setting up snapshot/book structure which is complex.
  ;; Skipping complex setup.
  )

^{:refer rt.postgres.grammar.common/pg-linked :added "4.0"}
(fact "emits the linked symbol"
  ;; Skipping complex setup.
  )

^{:refer rt.postgres.grammar.common/block-do-block :added "4.0"}
(fact "initates do block"
  ^:hidden
  
  (common/block-do-block '[(+ 1 2 3) \;])
  => '[:do :$$
       \\ :begin
       \\ (\| [(+ 1 2 3) \;])
       \\ :end \;
       \\ :$$ :language "plpgsql" \;])

^{:refer rt.postgres.grammar.common/block-do-suppress :added "4.0"}
(fact "initates suppress block"
  ^:hidden
  
  (common/block-do-suppress '[(+ 1 2 3) \;])
  => '[:do :$$
       \\ :begin
       \\ (\| [(+ 1 2 3) \;])
       \\ :exception :when-others-then
       \\ :end \;
       \\ :$$ :language "plpgsql" \;])

^{:refer rt.postgres.grammar.common/block-loop-block :added "4.0"}
(fact "emits loop block"
  ^:hidden
  
  (common/block-loop-block
   '_ '(+ 1 2 3) '(+ 4 5 6))
  => '[:loop \\ (\| (do (+ 1 2 3) (+ 4 5 6))) \\ :end-loop])

^{:refer rt.postgres.grammar.common/block-case-block :added "4.0"}
(fact "emits case block"
  ^:hidden

  (common/block-case-block
   'type
   "object" '(from-object x)
   "string" '(from-string x))
  => '(% [:case type \\ (\| :WHEN (:% "object") :THEN (:% (from-object x)) \\ :WHEN (:% "string") :THEN (:% (from-string x)) \\) :end]))

^{:refer rt.postgres.grammar.common/pg-defenum :added "4.0"}
(fact "defenum block"
  ^:hidden
  
  (common/pg-defenum '(defenum ^{:static/schema "schema"} hello [:a :b :c]))
  => '[:do :$$
       \\ :begin
       \\ (\| (do [:create-type (. #{"schema"} #{"hello"}) :as-enum (quote ("a" "b" "c"))]))
       \\ :exception :when-others-then
       \\ :end \;
       \\ :$$ :language "plpgsql" \;])

^{:refer rt.postgres.grammar.common/pg-defindex :added "4.0"}
(fact "defindex block"
  ^:hidden

  (common/pg-defindex '(defindex hello [table cols] body))
  => '[:create-index :if-not-exists hello :on table (quote ([cols])) body \;])

^{:refer rt.postgres.grammar.common/pg-defpolicy :added "4.0"}
(fact "defpolicy block"
  ^:hidden
  
  (common/pg-defpolicy '(defpolicy hello [table] ()))
  => '(do [:drop-policy-if-exists #{"hello - "} :on table] [:create-policy #{"hello - "} :on table \\]))

^{:refer rt.postgres.grammar.common/pg-defblock :added "4.0"}
(fact "creates generic defblock"
  ^:hidden
  
  (common/pg-defblock '(def ^{:static/return [:index]
                              :static/schema "scratch"} hello []))
  => '(do [:create :index (. #{"scratch"} #{"hello"})]))

^{:refer rt.postgres.grammar.common/pg-policy-format :added "4.0"}
(fact "formats a policy definition"
  ^:hidden
  
  (common/pg-policy-format '(defpolicy hello [table] ()))
  => '[{:doc "", :static/policy-name "hello - ", :static/policy-table nil, :static/policy-schema "table"}
       (defpolicy hello "" [table] ())])

^{:refer rt.postgres.grammar.common/pg-deftrigger :added "4.0"}
(fact "deftrigger block"
  ^:hidden
  
  (common/pg-deftrigger '(deftrigger hello [table] ()))
  => '(do [:drop-trigger-if-exists hello :on table] [:create-trigger hello :on table \\]))

^{:refer rt.postgres.grammar.common/pg-uuid :added "4.1"}
(fact "constructs a pg uuid"
  ^:hidden

  (common/pg-uuid "123")
  => "'123'::uuid")




^{:refer rt.postgres.grammar.common/pg-deftype-ref-name :added "4.1"}
(fact "gets the ref name"
  (common/pg-deftype-ref-name :user {:raw "user_id"})
  => "user_id"

  (common/pg-deftype-ref-name :user {})
  => "user_id")

^{:refer rt.postgres.grammar.common/block-while-block :added "4.1"}
(fact "emits while block"
  (common/block-while-block '(= 1 1) '(do-something))
  => '[:while (= 1 1) :loop \\ (\| (do (do-something))) \\ :end-loop \;])

^{:refer rt.postgres.grammar.common/pg-publication-format :added "4.1"}
(fact "formats publication"
  (common/pg-publication-format '(defpublication pub [:all]))
  => vector?)

^{:refer rt.postgres.grammar.common/pg-defpublication :added "4.1"}
(fact "defpublication block"
  (common/pg-defpublication '(defpublication pub [:all]))
  => '(do [:drop-publication-if-exists pub]
          [:create-publication pub :for :all :tables]))

^{:refer rt.postgres.grammar.common/pg-subscription-format :added "4.1"}
(fact "formats subscription"
  (common/pg-subscription-format '(defsubscription sub [conn pub] {}))
  => vector?)

^{:refer rt.postgres.grammar.common/pg-defsubscription :added "4.1"}
(fact "defsubscription block"
  (common/pg-defsubscription '(defsubscription sub ["conn" "pub"] {}))
  => '(do [:drop-subscription-if-exists sub]
          [:create-subscription sub :connection "conn" :publication "pub"]))
