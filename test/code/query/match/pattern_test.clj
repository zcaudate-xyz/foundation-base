(ns code.query.match.pattern-test
  (:require [code.query.match.impl :as match]
            [code.query.match.pattern :refer :all]
            [std.protocol.match :as protocol])
  (:use code.test))

^{:refer code.query.match.pattern/transform-pattern :added "3.0"}
(fact "converts an input into an actual matchable pattern"

  (transform-pattern ^:& #{:a :b})
  => (all match/actual-pattern?
          #(= (:expression %) #{:a :b}))

  (transform-pattern '^:% (symbol "_"))
  => (all match/eval-pattern?
          #(= (:expression %) '(symbol "_")))

  (transform-pattern #{:a :b})
  => #{:a :b}

  (transform-pattern [:a :b])
  => [:a :b]

  (transform-pattern [[:code {:a #'number?} #'string?]])
  => [[:code {:a #'clojure.core/number?}
       #'clojure.core/string?]])

^{:refer code.query.match.pattern/pattern-single-fn :added "3.0"}
(fact "creates a function based on template"

  ((pattern-single-fn '(a)) '(a))
  => true

  ((pattern-single-fn '(a)) '(b))
  => false)

^{:refer code.query.match.pattern/pattern-matches :added "3.0"}
(fact "pattern matches for a given template"
  ((pattern-matches ()) ())
  => '(())

  ((pattern-matches []) ())
  => ()

  ((pattern-matches '(^:% symbol? ^:? (+ 1 _ ^:? _))) '(+ (+ 1 2 3)))
  => '((^{:% true} symbol? ^{:? 0} (+ 1 _ ^{:? 1} _))))

^{:refer code.query.match.pattern/pattern-fn :added "3.0"}
(fact "make sure that functions are working properly"
  ((pattern-fn vector?) [])
  => true

  ((pattern-fn #'vector?) [])
  => true

  ((pattern-fn '^:% vector?) [])
  => true

  ((pattern-fn '^:% symbol?) [])
  => false

  ((pattern-fn '[^:% vector?]) [[]])
  => true

  ((pattern-fn [[:code map? string?]]) [[:code {} "hello"]])
  => true

  ((pattern-fn [[:code {:a number?} string?]]) [[:code {:a 1} "hello"]])
  => true

  ((pattern-fn [[:code {:a {:b number?}} string?]]) [[:code {:a {:b 1}} "hello"]])
  => true)

(comment
  (code.manage/import))
