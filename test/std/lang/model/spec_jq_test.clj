(ns std.lang.model.spec-jq-test
  (:use code.test)
  (:require [std.lang.model.spec-jq :refer :all]
            [std.lang.base.script :as script]
            [std.lib :as h]))

(script/script- :jq)

^{:refer std.lang.model.spec-jq/jq-args :added "4.0"}
(fact "custom args for jq"
  ^:hidden

  (jq-args '(1 2 3) +grammar+ {})
  => "(1; 2; 3)")

^{:refer std.lang.model.spec-jq/jq-invoke :added "4.0"}
(fact "outputs an invocation (same as vector)"
  ^:hidden

  (jq-invoke '(foo 1 2) +grammar+ {})
  => "foo(1; 2)")

^{:refer std.lang.model.spec-jq/jq-defn :added "4.0"}
(fact "transforms a function to allow for inputs"
  ^:hidden

  (jq-defn '(defn foo [x] x))
  => '(:% (k:def) (k:space) foo (:% (k:lparen) x (k:rparen)) (k:colon) (k:space) (do x) (k:semi)))

^{:refer std.lang.model.spec-jq/jq-as :added "4.0"}
(fact "jq variable binding"
  ^:hidden

  (jq-as '(as x))
  => '(:% (k:as) (k:space) (:$ x)))

^{:refer std.lang.model.spec-jq/jq-label :added "4.0"}
(fact "jq label"
  ^:hidden

  (jq-label '(label x))
  => '(:% (k:label) (k:space) (:$ x)))

^{:refer std.lang.model.spec-jq/jq-dot :added "4.0"}
(fact "jq dot access"
  ^:hidden

  (jq-dot '(. foo) +grammar+ {})
  => (symbol ".foo")

  (jq-dot '(. "foo") +grammar+ {})
  => '(:% "." "[" "foo" "]"))

^{:refer std.lang.model.spec-jq/jq-try :added "4.0"}
(fact "jq try/catch"
  ^:hidden

  (jq-try '(try (error "a") (error "b")))
  => '(:% (k:try) (k:space) (error "a") (k:space) (k:catch) (k:space) (error "b")))

^{:refer std.lang.model.spec-jq/jq-if :added "4.0"}
(fact "jq if/then/else"
  ^:hidden

  (jq-if '(if true "yes" "no"))
  => '(:% (k:if) (k:space) true (k:space) (k:then) (k:space) "yes" (k:space) (k:else) (k:space) "no" (k:space) (k:end)))

^{:refer std.lang.model.spec-jq/jq-reduce :added "4.0"}
(fact "jq reduce"
  ^:hidden

  (jq-reduce '(reduce [1 2 3] x 0 (+ . $x)))
  => '(:% (k:reduce) (k:space) [1 2 3] (k:space) (k:as) (k:space) (:$ x) (k:space)
          (:% (k:lparen) (% 0) (k:semi) (k:space) (% (+ . $x)) (k:rparen))))

^{:refer std.lang.model.spec-jq/jq-foreach :added "4.0"}
(fact "jq foreach"
  ^:hidden

  (jq-foreach '(foreach [1 2 3] x 0 (+ . $x)))
  => '(:% (k:foreach) (k:space) [1 2 3] (k:space) (k:as) (k:space) (:$ x) (k:space)
          (:% (k:lparen) (% 0) (k:semi) (k:space) (% (+ . $x)) (k:rparen)))

  (jq-foreach '(foreach [1 2 3] x 0 (+ . $x) (* . 2)))
  => '(:% (k:foreach) (k:space) [1 2 3] (k:space) (k:as) (k:space) (:$ x) (k:space)
          (:% (k:lparen) (% 0) (k:semi) (k:space) (% (+ . $x)) (k:semi) (k:space) (% (* . 2)) (k:rparen))))

(fact "basic emit tests"
  (!.jq
   (def foo [x]
     (+ x 1)))
  => "def foo(x): x + 1;"

  (!.jq
   (| . foo))
  => ". | foo"

  (!.jq
   (if (> . 5)
     "big"
     "small"))
  => "if . > 5 then \"big\" else \"small\" end"

  (!.jq
   (reduce inputs item 0
     (+ . $item)))
  => "reduce inputs as $item (0; . + $item)"

  (!.jq
   (try error .))
  => "try error catch ."

  (!.jq
   (| (label out) (break out)))
  => "label $out | break $out"

  (!.jq
   {:a 1 :b 2})
  => "{\"a\":1,\"b\":2}")


^{:refer std.lang.model.spec-jq/jq-args-ast :added "4.1"}
(fact "TODO")

^{:refer std.lang.model.spec-jq/jq-break :added "4.1"}
(fact "TODO")