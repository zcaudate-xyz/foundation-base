(ns std.lib.generate-test
  (:use code.test)
  (:require [std.lib.generate :refer :all]))

^{:refer std.lib.generate/macroexpand-code :added "3.0"}
(fact "macroexpand code, keeping the original meta"
  (macroexpand-code '(-> 1 inc))
  => '(inc 1))

^{:refer std.lib.generate/tag-visited :added "3.0"}
(fact "appends `:form/yield` to the form meta"
  (meta (tag-visited '()))
  => (contains {:form/yield true}))

^{:refer std.lib.generate/visit-sym :added "3.0"}
(fact "returns the visitor dispatch"

  (visit-sym '(if :a 1 2))
  => 'if

  (visit-sym '(yield 1))
  => 'yield

  (visit-sym '(std.lib.generate/yield 1))
  => 'yield)

^{:refer std.lib.generate/visited? :added "3.0"}
(fact "checks if form has been visted"

  (-> (tag-visited '(loop (+ 1 2 3)))
      visited?)
  => true)

^{:refer std.lib.generate/visit :added "3.0"}
(fact "testing inputs for visit" ^:hidden

  ;; do block
  (visit '(do :a :b :c))
  => '(do :a :b :c nil)

  ;; look block
  (visit '(loop* [:a :<val>]
                 (recur)))
  ;; ((fn loop67729 [:a] (clojure.core/lazy-seq (loop67729))) :<val>)
  => anything

  (visit '(if :a :true :false))
  => '(if :a :true :false)

  (visit '(let* [a 1
                 b 2]
                (yield (+ a b))))
  => '(let* [a 1 b 2] (list (+ a b)))

  (visit '(let* [a (loop [] (yield 1) (recur))
                 b 2]
                (yield (+ a b))))
  => '(let* [a (loop [] (yield 1)
                     (recur))
             b 2]
            (list (+ a b)))

  (visit '(letfn* [a identity
                   b identity]
                  (yield ((comp a b) 1))))
  => '(letfn* [a identity b identity] (list ((comp a b) 1)))

  (visit '(letfn* [a (fn [] (loop [] (yield 1) (recur)))
                   b (fn [] (loop [] (recur)))]
                  (list (a) (b) 1))))

^{:refer std.lib.generate/gen :added "3.0"}
(fact "returns a generator iteratively using yield"

  (gen (loop [i 10]
         (if-not (zero? i)
           (do (yield i)
               (recur (dec i))))))
  => '(10 9 8 7 6 5 4 3 2 1)

  (->> (gen (loop [i 0]
              (if (even? i)
                (yield i))
              (recur (inc i))))
       (take 5))
  => [0 2 4 6 8])

^{:refer std.lib.generate/yield :added "3.0"}
(fact "yields single value has to be used to within gen form"

  (yield 1)
  => (throws))

^{:refer std.lib.generate/yield-all :added "3.0"}
(fact "same as yield but returns entire seq" ^:hidden

  (gen (yield-all (for [i (range 10)]
                    i)))
  => '(0 1 2 3 4 5 6 7 8 9)

  (gen (let [xs (filter even? (range 10))
             ys (filter odd? (range 10))]
         (yield-all xs)
         (yield-all ys)))
  => [0 2 4 6 8 1 3 5 7 9])

(comment
  (visit '(do 1 2 3))
  => (do 1 2 3 nil)

  (visit '(loop* []
                 (yield 1)
                 (recur)))
  ((fn loop64387 [] (clojure.core/concat (lazy-seq (list 1)) (lazy-seq (clojure.core/lazy-seq (loop64387))))))

  (visit '(if :a (+ 1 2) (prn))))
