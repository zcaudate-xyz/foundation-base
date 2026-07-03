(ns hara.lang.rewrite.statement-test
  (:use code.test)
  (:require [hara.lang.rewrite.statement :as stmt]))

^{:refer hara.lang.rewrite.statement/rewrite-for-statement :added "4.1"}
(fact "rewrites a for statement binding and body"
  (stmt/rewrite-for-statement
   '(for [i (range 10)]
      (print i)
      (print i))
   (fn [form] (list 'bind form))
   (fn [body] (map (fn [form] (list 'stmt form)) body)))
  => '(for (bind [i (range 10)])
       (stmt (print i))
       (stmt (print i))))

^{:refer hara.lang.rewrite.statement/rewrite-cond-statement :added "4.1"}
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

^{:refer hara.lang.rewrite.statement/rewrite-branch-control :added "4.1"}
(fact "rewrites branch control tests and bodies"
  (stmt/rewrite-branch-control
   '(if (< x 0) (return -1))
   (fn [form] (list 'truthy form))
   (fn [body] (map (fn [form] (list 'stmt form)) body)))
  => '(if (truthy (< x 0)) (stmt (return -1)))

  (stmt/rewrite-branch-control
   '(else (return 0) (return 1))
   (fn [form] (list 'truthy form))
   (fn [body] (map (fn [form] (list 'stmt form)) body)))
  => '(else (stmt (return 0)) (stmt (return 1))))

^{:refer hara.lang.rewrite.statement/rewrite-branch-statement :added "4.1"}
(fact "rewrites each branch in a branch statement"
  (stmt/rewrite-branch-statement
   '(branches (if (< x 0) (return -1)) (else (return 0)))
   (fn [branch]
     (stmt/rewrite-branch-control
      branch
      (fn [form] (list 'truthy form))
      (fn [body] (map (fn [form] (list 'stmt form)) body)))))
  => '(br* (if (truthy (< x 0)) (stmt (return -1)))
       (else (stmt (return 0)))))

^{:refer hara.lang.rewrite.statement/rewrite-do-statement :added "4.1"}
(fact "rewrites a do statement body and splices nested do* forms"
  (stmt/rewrite-do-statement
   '(do (step-a) (step-b))
   (fn [body] (map (fn [form] (list 'stmt form)) body)))
  => '(do (stmt (step-a)) (stmt (step-b)))

  (stmt/rewrite-do-statement
   '(do (step-a) (step-b))
   (fn [body] (map (fn [form] (list 'stmt form)) body))
   (fn [body] (cons '(setup) body)))
  => '(do (stmt (setup)) (stmt (step-a)) (stmt (step-b)))

  (stmt/rewrite-do-statement
   '(do (a) (b))
   (fn [body]
     [(list 'do* (list 'stmt (first body)) (list 'stmt (second body)))]))
  => '(do (stmt (a)) (stmt (b))))

^{:refer hara.lang.rewrite.statement/rewrite-var-statement :added "4.1"}
(fact "rewrites the bound expression in a var statement"
  (stmt/rewrite-var-statement '(var x) (fn [form] (list 'expr form)))
  => '(var x)

  (stmt/rewrite-var-statement '(var x 1 2 (+ 1 2)) (fn [form] (list 'expr form)))
  => '(var x 1 2 (expr (+ 1 2))))

^{:refer hara.lang.rewrite.statement/rewrite-return-statement :added "4.1"}
(fact "rewrites each expression in a return statement"
  (stmt/rewrite-return-statement
   '(return (+ 1 2) (* 3 4))
   (fn [form] (list 'expr form)))
  => '(return (expr (+ 1 2)) (expr (* 3 4))))

^{:refer hara.lang.rewrite.statement/rewrite-if-statement :added "4.1"}
(fact "rewrites an if statement test and branches"
  (stmt/rewrite-if-statement
   '(if (< x 0) (return -1))
   (fn [form] (list 'truthy form))
   (fn [form] (list 'stmt form)))
  => '(if (truthy (< x 0)) (stmt (return -1)))

  (stmt/rewrite-if-statement
   '(if (< x 0) (return -1) (return 1))
   (fn [form] (list 'truthy form))
   (fn [form] (list 'stmt form)))
  => '(if (truthy (< x 0)) (stmt (return -1)) (stmt (return 1))))

^{:refer hara.lang.rewrite.statement/rewrite-when-statement :added "4.1"}
(fact "rewrites a when statement test and body"
  (stmt/rewrite-when-statement
   '(when (< x 0) (print x) (return x))
   (fn [form] (list 'truthy form))
   (fn [body] (map (fn [form] (list 'stmt form)) body)))
  => '(when (truthy (< x 0)) (stmt (print x)) (stmt (return x))))

^{:refer hara.lang.rewrite.statement/rewrite-while-statement :added "4.1"}
(fact "rewrites a while statement test and body"
  (stmt/rewrite-while-statement
   '(while (< x 10) (print x) (inc x))
   (fn [form] (list 'truthy form))
   (fn [body] (map (fn [form] (list 'stmt form)) body)))
  => '(while (truthy (< x 10)) (stmt (print x)) (stmt (inc x))))

^{:refer hara.lang.rewrite.statement/rewrite-defn-statement :added "4.1"}
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
