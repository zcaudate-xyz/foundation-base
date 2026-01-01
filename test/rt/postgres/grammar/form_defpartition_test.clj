(ns rt.postgres.grammar.form-defpartition-test
  (:use code.test)
  (:require [rt.postgres.grammar.form-defpartition :as form-defpartition]
            [rt.postgres.grammar.common :as common] ;; for pg-deftype-ref-name used inside
            [std.lib :as h]))

^{:refer rt.postgres.grammar.form-defpartition/pg-partition-name :added "4.1"}
(fact "constructs partition name"
  ^:hidden
  
  (form-defpartition/pg-partition-name "table" "val" ["stack"])
  => "table__stack__val")

^{:refer rt.postgres.grammar.form-defpartition/pg-partition-def :added "4.1"}
(fact "recursive definition for partition"
  ^:hidden
  
  (form-defpartition/pg-partition-def 'parent "base" {:use :col :in ["a"]} [] [])
  => '([:create-table :if-not-exists #{"base__a"} :partition-of #{"parent"} :for :values :in (quote ("a"))]))


^{:refer rt.postgres.grammar.form-defpartition/pg-defpartition :added "4.1"}
(fact "defpartition block"
  ^:hidden

  (form-defpartition/pg-defpartition '(defpartition part [parent] [{:use :col :in ["a"]}]))
  => '(do [:create-table :if-not-exists #{"parent__a"} :partition-of #{"parent"} :for :values :in (quote ("a"))])

  (fact "defpartition handles multiple parents"
    (form-defpartition/pg-defpartition '(defpartition part [p1 p2] [{:use :col :in ["a"]}]))
    => '(do [:create-table :if-not-exists #{"p1__a"} :partition-of #{"p1"} :for :values :in (quote ("a"))]
            [:create-table :if-not-exists #{"p2__a"} :partition-of #{"p2"} :for :values :in (quote ("a"))]))

  (fact "defpartition handles multiple parents with nested partitions"
    (form-defpartition/pg-defpartition '(defpartition part [p1 p2] 
                                          [{:use :c1 :in ["v1"]}
                                           {:use :c2 :in ["v2"]}]))
    => '(do [:create-table :if-not-exists #{"p1__v1"} :partition-of #{"p1"} :for :values :in (quote ("v1")) :partition-by :list (quote (c2))]
            [:create-table :if-not-exists #{"p1__v1__v2"} :partition-of #{"p1__v1"} :for :values :in (quote ("v2"))]
            [:create-table :if-not-exists #{"p2__v1"} :partition-of #{"p2"} :for :values :in (quote ("v1")) :partition-by :list (quote (c2))]
            [:create-table :if-not-exists #{"p2__v1__v2"} :partition-of #{"p2__v1"} :for :values :in (quote ("v2"))]))
  
  (fact "defpartition handles default partition"
    (form-defpartition/pg-defpartition '(defpartition part [p1] [{:default true}]))
    => '(do [:create-table :if-not-exists #{"p1__$DEFAULT"} :partition-of #{"p1"} :default]))

  (fact "defpartition handles nested partitions and schemas"
    (form-defpartition/pg-defpartition 
     '(defpartition RevPartitionBase
        [szn_type/Rev]
        [{:use :class :schema "szn_type" :in ["Default"]}
         {:use :class-table :schema "szn_type_rev" :in ["Feed"]}]))
    => '(do [:create-table :if-not-exists (. #{"szn_type"} #{"Rev__Default"})
             :partition-of (. #{"szn_type"} #{"Rev"})
             :for :values :in (quote ("Default"))
             :partition-by :list (quote (class_table))]
            [:create-table :if-not-exists (. #{"szn_type_rev"} #{"Rev__Default__Feed"})
             :partition-of (. #{"szn_type"} #{"Rev__Default"})
             :for :values :in (quote ("Feed"))]))

  (fact "defpartition handles :on and :for keywords"
    (form-defpartition/pg-defpartition
     '(defpartition.pg RevPartitionGlobal
        [szn_type/Rev]
        [{:on   :class
          :for  ["Global"]
          :schema "szn_type"}
         {:on   :class
          :for  ["Token" "Commodity"]
          :schema "szn_type_rev"}]))
    => '(do [:create-table :if-not-exists (. #{"szn_type"} #{"Rev__Global"})
             :partition-of (. #{"szn_type"} #{"Rev"})
             :for :values :in (quote ("Global"))
             :partition-by :list (quote (class))]
            [:create-table :if-not-exists (. #{"szn_type_rev"} #{"Rev__Global__Token"})
             :partition-of (. #{"szn_type"} #{"Rev__Global"})
             :for :values :in (quote ("Token"))]
            [:create-table :if-not-exists (. #{"szn_type_rev"} #{"Rev__Global__Commodity"})
             :partition-of (. #{"szn_type"} #{"Rev__Global"})
             :for :values :in (quote ("Commodity"))])))
