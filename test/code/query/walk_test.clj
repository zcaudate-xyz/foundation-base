(ns code.query.walk-test
  (:use code.test)
  (:require [code.query.walk :refer :all]
            [code.query.match :as match]
            [code.edit :as nav]))

^{:refer code.query.walk/wrap-meta :added "3.0"}
(fact "allows matchwalk to handle meta tags")

^{:refer code.query.walk/wrap-suppress :added "3.0"}
(fact "allows matchwalk to handle exceptions")

^{:refer code.query.walk/matchwalk :added "3.0"}
(fact "match every entry within a form"

  (-> (matchwalk (nav/parse-string "(+ (+ (+ 8 9)))")
                 [(match/compile-matcher '+)]
                 (fn [nav]
                   (-> nav nav/down (nav/replace '-) nav/up)))
      nav/value)
  => '(- (- (- 8 9))))

^{:refer code.query.walk/levelwalk :added "3.0"}
(fact "only match the form at the top level"
  (-> (levelwalk (nav/parse-string "(+ (+ (+ 8 9)))")
                 [(match/compile-matcher '+)
                  (match/compile-matcher '+)]
                 (fn [nav]
                   (-> nav nav/down (nav/replace '-) nav/up)))
      nav/value)
  => '(- (+ (+ 8 9))))
