(ns rt.postgres.grammar.form-defpartition-test
  (:use code.test)
  (:require [rt.postgres.grammar.form-defpartition :as form-defpartition]
            [rt.postgres.grammar.common :as common] ;; for pg-deftype-ref-name used inside
            [std.lib :as h]))

^{:refer rt.postgres.grammar.form-defpartition/pg-partition-name :added "4.1"}
(fact "constructs partition name"
  ^:hidden
  
  (form-defpartition/pg-partition-name "table" "val" ["stack"])
  => "table_val_stack")

^{:refer rt.postgres.grammar.form-defpartition/pg-partition-quote-id :added "4.1"}
(fact "quotes an identifier if needed"
  ^:hidden
  
  (form-defpartition/pg-partition-quote-id "id")
  => "\"id\"")

^{:refer rt.postgres.grammar.form-defpartition/pg-partition-full-name :added "4.1"}
(fact "constructs partition full name"
  ^:hidden
  
  (form-defpartition/pg-partition-full-name "schema" "table")
  => "schema.\"table\""

  (form-defpartition/pg-partition-full-name nil "table")
  => "\"table\"")

^{:refer rt.postgres.grammar.form-defpartition/pg-partition-def :added "4.1"}
(fact "recursive definition for partition"
  ^:hidden
  
  (form-defpartition/pg-partition-def 'parent "base" {:use :col :in ["a"]} [] [])
  => '([:create-table :if-not-exists (raw "\"base_a\"") :partition-of (raw "\"base\"") :for :values :in (quote ("a"))]))

^{:refer rt.postgres.grammar.form-defpartition/pg-defpartition :added "4.1"}
(fact "defpartition block"
  ^:hidden

  (form-defpartition/pg-defpartition '(defpartition part [parent] [{:use :col :in ["a"]}]))
  => '(do [:create-table :if-not-exists (raw "\"parent_a\"") :partition-of (raw "\"parent\"") :for :values :in (quote ("a"))]))

^{:refer rt.postgres.grammar.form-defpartition/pg-deftype-partition :added "4.1"}
(fact "creates partition by statement"
  ^:hidden
  
  (form-defpartition/pg-deftype-partition {:partition-by [:range :abc-def]}
                                          [[:abc-def {:type :time}]])
  => '(:partition-by :range (quote ("abc_def")))

  (let [colspec [[:id {:type :uuid, :primary "default", :sql {:default '(uuid-generate-v4)}, :scope :-/id}]
                 [:class {:type :enum, :required true, :scope :-/info, :primary "default", :enum {:ns 'szndb.core.type-seed/EnumClassType}, :sql {:unique ["class"]}}]
                 [:class-table {:type :enum, :required true, :scope :-/info, :primary "primary", :enum {:ns 'szndb.core.type-seed/EnumTableType}, :sql {:unique ["class"]}}]
                 [:class-ref {:type :uuid, :required true, :sql {:unique ["class"]}, :scope :-/data}]
                 [:index {:type :integer, :required true, :scope :-/info, :sql {:default 0}}]
                 [:current {:type :map, :sql {:default "{}"}, :scope :-/data}]
                 [:op-created {:type :uuid, :scope :-/data}]
                 [:op-updated {:type :uuid, :scope :-/data}]
                 [:time-created {:type :time, :scope :-/data}]
                 [:time-updated {:type :time, :scope :-/data}]]]
    (form-defpartition/pg-deftype-partition {:partition-by {:strategy :list :columns [:class]}}
                                            colspec))
  => '(:partition-by :list (quote ("class")))

  (fact "pg-deftype-partition handles sets in column list"
    (form-defpartition/pg-deftype-partition {:partition-by [:list #{"class"}]} [])
    => '(:partition-by :list (quote ("class"))))

  (fact "pg-deftype-partition handles map format"
    (form-defpartition/pg-deftype-partition {:partition-by {:strategy :list :columns [:class]}} [])
    => '(:partition-by :list (quote ("class"))))

  (fact "pg-deftype-partition should error if column is not found"
    (form-defpartition/pg-deftype-partition {:partition-by {:strategy :list :columns [:wrong-column]}} 
                                            [[:class {:type :text}]])
    => (throws)))