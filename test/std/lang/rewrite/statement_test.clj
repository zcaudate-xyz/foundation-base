(ns std.lang.rewrite.statement-test
  (:use code.test)
  (:require [std.lang.rewrite.statement :as stmt]))

^{:refer std.lang.rewrite.statement/rewrite-for-statement :added "4.1"}
(fact "TODO")

^{:refer std.lang.rewrite.statement/rewrite-cond-statement :added "4.1"}
(fact "rewrites conditional statement skeletons with injected callbacks"
  (stmt/rewrite-cond-statement
   '(cond ready
      (return 1)
      :else
      (return 2))
   (fn [form] (list 'truthy form))
   (fn [form] (list 'stmt form)))
  => '(cond (truthy ready)
       (stmt (return 1))
       :else
       (stmt (return 2))))

^{:refer std.lang.rewrite.statement/rewrite-branch-control :added "4.1"}
(fact "TODO")

^{:refer std.lang.rewrite.statement/rewrite-branch-statement :added "4.1"}
(fact "TODO")

^{:refer std.lang.rewrite.statement/rewrite-do-statement :added "4.1"}
(fact "TODO")

^{:refer std.lang.rewrite.statement/rewrite-var-statement :added "4.1"}
(fact "TODO")

^{:refer std.lang.rewrite.statement/rewrite-return-statement :added "4.1"}
(fact "TODO")

^{:refer std.lang.rewrite.statement/rewrite-if-statement :added "4.1"}
(fact "TODO")

^{:refer std.lang.rewrite.statement/rewrite-when-statement :added "4.1"}
(fact "TODO")

^{:refer std.lang.rewrite.statement/rewrite-while-statement :added "4.1"}
(fact "TODO")

^{:refer std.lang.rewrite.statement/rewrite-defn-statement :added "4.1"}
(fact "rewrites function statement bodies with optional finalization"
  (stmt/rewrite-defn-statement
   '(defn sample [x]
      (step-a)
      (step-b))
   (fn [body]
     (map (fn [form] (list 'stmt form)) body))
   (fn [body]
     (concat (butlast body)
             [(list 'return (last body))])))
  => '(defn sample [x]
       (do
         (stmt (step-a))
         (return (stmt (step-b))))))