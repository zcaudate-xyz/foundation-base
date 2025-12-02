(ns code.test.compile.rewrite-test
  (:use code.test)
  (:require [code.test.compile.rewrite :refer :all]))

(defn is-check? [form val]
  (let [is-seq (sequential? form)
        fst (first form)
        is-sym (= fst 'code.test.base.process/process)]
    (if (and is-seq is-sym)
      (let [m (second form)]
        (and (= (:type m) :test-equal)
             (= (get-in m [:input :form]) (list 'quote val))
             (if (:output m)
               (= (get-in m [:output :form]) (list 'quote val))
               true)))
      (do (prn "CHECK FAIL:" form "Seq?" is-seq "Sym?" fst "Eq?" is-sym)
          false))))

^{:refer code.test.compile.rewrite/rewrite-list :added "4.0"}
(fact "rewrites a list containing checks"
  (rewrite-list '(1 => 1))
  => (satisfies
      (fn [res]
        (and (= (count res) 1)
             (is-check? (first res) 1))))

  (rewrite-list '(1 => 1 2 => 2))
  => (satisfies
      (fn [res]
        (and (= (count res) 2)
             (is-check? (first res) 1)
             (is-check? (second res) 2))))

  (rewrite-list '(1 2 3))
  => '(1 2 3))

^{:refer code.test.compile.rewrite/rewrite-nested-checks :added "4.1"}
(fact "rewrites code to replace `a => b` with check calls"
  (rewrite-nested-checks '(1 => 1))
  => (satisfies
      (fn [res]
        (and (= (count res) 1)
             (is-check? (first res) 1))))

  (rewrite-nested-checks '((1 => 1)))
  => (satisfies
      (fn [res]
        (let [inner (first res)]
          (and (= (count inner) 1)
               (is-check? (first inner) 1)))))

  ;; Vectors are not rewritten if they contain => directly, as they are not forms (lists)
  (rewrite-nested-checks '[1 => 1])
  => '[1 => 1]

  ;; Maps are rewritten if values are lists with =>
  (rewrite-nested-checks '{a (1 => 1)})
  => (satisfies
      (fn [res]
        (let [inner (get res 'a)]
          (and (= (count inner) 1)
               (is-check? (first inner) 1)))))
  
  ;; Nested vectors with lists are rewritten
  (rewrite-nested-checks '[(1 => 1)])
  => (satisfies
      (fn [res]
        (let [inner (first res)]
          (and (= (count inner) 1)
               (is-check? (first inner) 1))))))

(fact "nested facts are not rewritten"
  (rewrite-nested-checks '(fact (1 => 1)))
  => '(fact (1 => 1)))

