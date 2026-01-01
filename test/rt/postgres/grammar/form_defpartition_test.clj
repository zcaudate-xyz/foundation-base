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

^{:refer rt.postgres.grammar.form-defpartition/pg-partition-def :added "4.1"}
(fact "recursive definition for partition"
  ^:hidden
  
  (form-defpartition/pg-partition-def 'parent "base" {:use :col :in ["a"]} [] [])
  => '([:create-table :if-not-exists #{"base_a"} :partition-of #{"base"} :for :values :in (quote ("a"))]))

^{:refer rt.postgres.grammar.form-defpartition/pg-defpartition :added "4.1"}
(fact "defpartition block"
  ^:hidden

  (form-defpartition/pg-defpartition '(defpartition part [parent] [{:use :col :in ["a"]}]))
  => '(do [:create-table :if-not-exists #{"parent_a"} :partition-of #{"parent"} :for :values :in (quote ("a"))]))