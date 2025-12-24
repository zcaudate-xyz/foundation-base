(ns rt.postgres.grammar.form-let-test
  (:use code.test)
  (:require [rt.postgres.grammar.form-let :refer :all]
            [std.string :as str]
            [std.lib :as h]
            [std.lang :as l]
            [rt.postgres.grammar.common :as common]
            [std.lang.base.emit-common :as emit-common]))

^{:refer rt.postgres.grammar.form-let/pg-tf-let-block :added "4.0"}
(fact "transforms a let block call"
  ^:hidden

  (pg-tf-let-block '(let:block {:name hello
                                :declare [(:int v := 8)
                                          (:int m 0)]}
                      (return (+ v m))))
  => '(do (\\ (\| "<<" hello ">>"))
          [:declare
           \\
           (\| (do (var :int v := 8) (var :int m 0)))
           \\ :begin
           \\ (\| (do (return (+ v m))))
           \\ :end]))

^{:refer rt.postgres.grammar.form-let/pg-tf-let-assign :added "4.0"}
(fact "create assignment statments for let form"
  ^:hidden
  
  (pg-tf-let-assign '[sym (hello)])
  => '[(:= sym (hello))]
  
  (pg-tf-let-assign '[sym [:select * :from table]])
  => '[[:select * :from table :into sym]])

^{:refer rt.postgres.grammar.form-let/pg-tf-let-check-body :added "4.0"}
(fact "checks if variables are in scope"
  (pg-tf-let-check-body '#{a} '(do (:= a 1)))
  => nil

  ;; Nested let should pass
  (pg-tf-let-check-body '#{v-a} '[(let [v-b 2] (:= v-b (+ v-a v-b)))])
  => nil

  ;; :for should pass IF the variable is declared (PL/pgSQL semantics)
  (pg-tf-let-check-body '#{v-a i-emails v-email} '[[:for v-email :in :select (elements i-emails)
                                                   (loop [] (:= v-a v-email))]])
  => nil

  ;; This should fail because 'v-c' is truly unknown
  (try
    (pg-tf-let-check-body '#{v-a} '[(let [v-b 2] (:= v-c (+ v-a v-b)))])
    (catch Throwable e (str/includes? (str e) "Unknown symbols in form")))
  => true)

^{:refer rt.postgres.grammar.form-let/pg-tf-let :added "4.0"}
(fact "creates a let form"
  ^:hidden
  
  (pg-tf-let '(let [(:int a) 1
                    (:int b) 2
                    _ (:= a (+ a b))]
                (return a)))
  => '(do [:declare
           \\ (\| (do (var :int a)
                      (var :int b)))
           \\ :begin
           \\ (\| (do (:= a 1)
                      (:= b 2)
                      (:= a (+ a b))
                      (return a)))
           \\ :end])
  
  (pg-tf-let '(let [(:int a) 1]
                (let [(:int b) 2]
                  (return (+ a b)))))
  => '(do [:declare \\ (\| (do (var :int a))) \\ :begin \\ (\| (do (:= a 1) (let [(:int b) 2] (return (+ a b))))) \\ :end]))

^{:refer rt.postgres.grammar.form-let/pg-do-block :added "4.0"}
(fact "emits a block with let usage"
  (with-redefs [emit-common/*emit-fn* (fn [& _] "block")]
    (pg-do-block '(do:block (let [a 1] (return a))) nil nil))
  => "block")

^{:refer rt.postgres.grammar.form-let/pg-do-suppress :added "4.0"}
(fact "emits a suppress block with let ussage"
  (with-redefs [emit-common/*emit-fn* (fn [& _] "block")]
    (pg-do-suppress '(do:block (let [a 1] (return a))) nil nil))
  => "block")


^{:refer rt.postgres.grammar.form-let/pg-loop-block :added "4.0"}
(fact "creates a loop block"
  (with-redefs [emit-common/*emit-fn* (fn [& _] "block")]
    (pg-loop-block '(loop [] (let [a 1] (return a))) nil nil))
  => "block")

^{:refer rt.postgres.grammar.form-let/pg-case-block :added "4.0"}
(fact "creates a case block"
  (with-redefs [emit-common/*emit-fn* (fn [& _] "block")]
    (pg-case-block '(case type "a" "b") nil nil))
  => "block")
