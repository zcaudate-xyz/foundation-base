(ns code.query-test
  (:use code.test)
  (:require [code.query :refer :all]
            [code.edit :as nav]))

^{:refer code.query/match :added "3.0"}
(fact "matches the source code"
  ^:hidden
  
  (match (nav/parse-string "(+ 1 1)") '(symbol? _ _))
  => false

  (match (nav/parse-string "(+ 1 1)") '(^:% symbol? _ _))
  => true

  (match (nav/parse-string "(+ 1 1)") '(^:%- symbol? _ | _))
  => true

  (match (nav/parse-string "(+ 1 1)") '(^:%+ symbol? _ _))
  => false)

^{:refer code.query/traverse :added "3.0"}
(fact "uses a pattern to traverse as well as to edit the form"
  ^:hidden
  
  (nav/value
   (traverse (nav/parse-string "^:a (+ () 2 3)")
             '(+ () 2 3)))
  => '(+ () 2 3)

  (nav/value
   (traverse (nav/parse-string "()")
             '(^:&+ hello)))
  => '(hello)

  (nav/value
   (traverse (nav/parse-string "()")
             '(+ 1 2 3)))
  => (throws)

  ($ {:string "(ns hello) ()"}
      [(ns _ ^:%+ (keyword "oeuoeuoe"))])
  => '[(ns hello :oeuoeuoe)]
  
  (nav/value
   (traverse (nav/parse-string "(defn hello \"world\" {:a 1} [])")
             '(defn ^:% symbol? ^:?%- string? ^:?%- map? ^:% vector? & _)))
  => '(defn hello []))

^{:refer code.query/select :added "3.0"}
(fact "selects all patterns from a starting point"
  ^:hidden
  
  (map nav/value
       (select (nav/parse-root "(defn hello [] (if (try))) (defn hello2 [] (if (try)))")
               '[defn if try]))
  => '((defn hello  [] (if (try)))
       (defn hello2 [] (if (try)))))

^{:refer code.query/modify :added "3.0"}
(fact "modifies location given a function"
  ^:hidden
  
  (nav/string
   (modify (nav/parse-root "^:a (defn hello3) (defn hello)") ['(defn | _)]
           (fn [zloc]
             (nav/insert-token-to-left zloc :hello))))
  => "^:a (defn :hello hello3) (defn :hello hello)")

^{:refer code.query/context-zloc :added "3.0"}
(fact "gets the context for loading forms"
  ^:hidden
  
  (context-zloc (nav/parse-string "1"))
  => (comp nav/navigator?))

^{:refer code.query/wrap-vec :added "3.0"}
(fact "ensures an item is wrapped in a vector, or handles it as a vector if it already is one"
  ^:hidden
  
  ((wrap-vec (fn [x opts] x)) [1] {}) => [1]
  ((wrap-vec (fn [x opts] x)) 1 {}) => 1)

^{:refer code.query/wrap-return :added "3.0"}
(fact "decides whether to return a string, zipper or sexp representation`"
  ^:hidden
  
  ((wrap-return (fn [res opts] res)) (nav/parse-string "1") {:return :string})
  => "1")

^{:refer code.query/$* :added "3.0"}
(fact "provides a lower-level interface for selecting and manipulating Clojure source code, used as a helper for `$`")

^{:refer code.query/$ :added "3.0"}
(fact "select and manipulation of clojure source code"
  ^:hidden
  
  ($ {:string "(defn hello1) (defn hello2)"}
      [(defn _ ^:%+ (keyword "oeuoeuoe"))])
  => '[(defn hello1 :oeuoeuoe) (defn hello2 :oeuoeuoe)]

  ($ {:string "(defn hello1) (defn hello2)"}
      [(defn _ | ^:%+ (keyword "oeuoeuoe"))])
  => '[:oeuoeuoe :oeuoeuoe]

  (->> ($ {:string "(defn hello1) (defn hello2)"}
          [(defn _ | ^:%+ (keyword "oeuoeuoe"))]
          {:return :string}))
  => [":oeuoeuoe" ":oeuoeuoe"]

  (first ($ (nav/parse-root "a b c") [{:is a}]))
  => 'a)

(comment
  (def fragment {:string "(defn hello [] (println \"hello\"))\n
                          (defn world [] (if true (prn \"world\")))"})

  (fact
    ($ fragment [defn 2 prn])
    => '[(defn world [] (if true (prn "world")))]

    (code.query.compile/prepare '[defn :2 prn])

    ($* fragment [{:form 'defn,
                   :nth-contains [2 {:form 'prn}]}])

    ($* fragment [{:form 'defn,
                   :nth-contains [1 {:form 'prn}]}])

    ($ fragment [defn 3 prn])
    => '[]))
