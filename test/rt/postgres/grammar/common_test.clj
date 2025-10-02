(ns rt.postgres.grammar.common-test
  (:use code.test)
  (:require [rt.postgres.grammar.common :as common]
            [rt.postgres.grammar :as g]
            [rt.postgres.script.scratch :as scratch]
            [rt.postgres.script.builtin :as builtin]
            [std.lang :as l]
            [std.lib :as h]))

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
  => '[{:- [:int],
        :static/return [:int],
        :static/language :default,
        :static/props [],}
       (def hello 1)])

^{:refer rt.postgres.grammar.common/pg-hydrate-module-static :added "4.0"}
(fact "gets the static module"
  ^:hidden
  
  (common/pg-hydrate-module-static
   (l/get-module (l/runtime-library)
                 :postgres
                 'rt.postgres.script.scratch))
  => #:static{:schema "scratch", :application ["scratch"]})

^{:refer rt.postgres.grammar.common/pg-hydrate :added "4.0"}
(fact "hydrate function for top level entries")

^{:refer rt.postgres.grammar.common/pg-string :added "4.0"}
(fact "constructs a pg string"
  ^:hidden
  
  (common/pg-string "hello")
  ="'hello'"

  (common/pg-string "'hello'")
  => "'''hello'''")

^{:refer rt.postgres.grammar.common/pg-map :added "4.0"}
(fact "creates a postgres json object"
  ^:hidden
  
  (common/pg-map {:a [1 2 3] :b 4}
                 g/+grammar+
                 {})
  => "(jsonb-build-object \"a\" (jsonb-build-array 1 2 3) \"b\" 4)")

^{:refer rt.postgres.grammar.common/pg-set :added "4.0"}
(fact "makes a set object"
  ^:hidden
  
  (common/pg-set #{"hello"}
                 g/+grammar+
                 {})
  => "\"hello\"")

^{:refer rt.postgres.grammar.common/pg-array :added "4.0"}
(fact "creates an array object"

  (common/pg-array '(array 1 2 3 4 5)
                   g/+grammar+
                   {})
  => "ARRAY[1,2,3,4,5]")

^{:refer rt.postgres.grammar.common/pg-invoke-typecast :added "4.0"}
(fact  "emits a typecast call"
  ^:hidden
  
  (common/pg-invoke-typecast '(:int [] (3) arr)
                      g/+grammar+
                      {})
  => "(arr)::INT[](3)")

^{:refer rt.postgres.grammar.common/pg-typecast :added "4.0"}
(fact "creates a typecast"
  ^:hidden
  
  (common/pg-typecast '(++ arr :int [] (3))
                      g/+grammar+
                      {})
  => "(arr)::INT[](3)")

^{:refer rt.postgres.grammar.common/pg-do-assert :added "4.0"}
(fact "creates an assert form"
  ^:hidden
  
  (common/pg-do-assert '(do:assert
                         (= 1 1)
                         [:tag {}])
                       g/+grammar+
                       {})
  => "(if [:NOT (quote ((= 1 1)))] [:raise-exception :using-detail := (% {:status \"error\", :tag :tag})])")

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

^{:refer rt.postgres.grammar.common/pg-entry-token :added "4.0"}
(fact "gets the entry token"
  ^:hidden
  
  (common/pg-entry-token @scratch/addf)
  => '(. #{"scratch"} addf))

^{:refer rt.postgres.grammar.common/pg-linked-token :added "4.0"}
(fact "gets the linked token given symbol"
  ^:hidden
  
  (common/pg-linked-token `scratch/addf
                   {:lang :postgres
                    :snapshot (l/get-snapshot (l/runtime-library))})
  => '(. #{"scratch"} addf)

  (common/pg-linked-token 'rt.postgres/abs
                   {:lang :postgres
                    :snapshot (l/get-snapshot (l/runtime-library))})
  => 'abs)

^{:refer rt.postgres.grammar.common/pg-linked :added "4.0"}
(fact "emits the linked symbol"
  ^:hidden
  
  (common/pg-linked `scratch/addf g/+grammar+
                    {:lang :postgres
                     :snapshot (l/get-snapshot (l/runtime-library))})
  => "(. #{\"scratch\"} addf)"

  (common/pg-linked 'rt.postgres/abs g/+grammar+
                    {:lang :postgres
                     :snapshot (l/get-snapshot (l/runtime-library))})
  => "abs")

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

^{:refer rt.postgres.grammar.common/pg-defenum :added "4.0"}
(fact "defenum block"
  ^:hidden
  
  (common/pg-defenum '(defenum hello [:a :b :c]))
  => '[:do :$$
       \\ :begin
       \\ (\| (do [:create-type #{"hello"} :as-enum (quote ("a" "b" "c"))]))
       \\ :exception :when-others-then
       \\ :end \;
       \\ :$$ :language "plpgsql" \;])

^{:refer rt.postgres.grammar.common/pg-defindex :added "4.0"}
(fact "defindex block"
  ^:hidden
  
  (common/pg-defindex '(defindex hello []))
  => '(do [:create-index :if-not-exists #{"hello"}]))

^{:refer rt.postgres.grammar.common/pg-defblock :added "4.0"}
(fact "creates generic defblock"
  ^:hidden
  
  (common/pg-defblock '(def ^{:static/return [:index]
                       :static/schema "scratch"} hello []))
  => '(do [:create :index (. #{"scratch"} #{"hello"})]))


^{:refer rt.postgres.grammar.common/pg-entry-literal :added "4.0"}
(fact "TODO")

^{:refer rt.postgres.grammar.common/block-loop-block :added "4.0"}
(fact "TODO")

^{:refer rt.postgres.grammar.common/block-case-block :added "4.0"}
(fact "TODO")