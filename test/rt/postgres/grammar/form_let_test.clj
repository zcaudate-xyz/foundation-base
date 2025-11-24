(ns rt.postgres.grammar.form-let-test
  (:use code.test)
  (:require [rt.postgres.grammar.form-let :refer :all]
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
  => nil)

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
