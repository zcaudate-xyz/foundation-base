(ns code.query.walk-test
  (:use code.test)
  (:require [code.query.walk :refer :all]
            [code.query.match :as match]
            [std.block.navigate :as nav]))

^{:refer code.query.walk/wrap-meta :added "3.0"}
(fact "allows matchwalk to handle meta tags"
  (let [f (wrap-meta (fn [nav _ _ _ _] nav))]
    (f (nav/parse-string "^:x ()") nil nil nil nil)
    => (satisfies nav/navigator?)))

^{:refer code.query.walk/wrap-suppress :added "3.0"}
(fact "allows matchwalk to handle exceptions"
  (let [f (wrap-suppress (fn [_ _ _ _ _] (throw (Exception.))))]
    (f :nav nil nil nil nil)
    => :nav))

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
