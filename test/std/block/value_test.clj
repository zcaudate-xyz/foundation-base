(ns std.block.value-test
  (:use code.test)
  (:require [std.block.value :refer :all]
            [std.block.base :as base]
            [std.block.construct :as construct]
            [std.block.parse :as parse]))

^{:refer std.block.value/apply-modifiers :added "3.0"}
(fact "applys the modifiers within a container"

  (apply-modifiers [(construct/uneval)
                    (construct/uneval)
                    1 2 3])
  => [3])

^{:refer std.block.value/child-values :added "3.0"}
(fact "returns child values of a container"

  (child-values (parse/parse-string "[1 #_2 3]"))
  => [1 3])

^{:refer std.block.value/root-value :added "3.0"}
(fact "returns the value of a :root block"

  (root-value (parse/parse-root "1 2 3"))
  => '(do 1 2 3))

^{:refer std.block.value/from-value-string :added "3.0"}
(fact "reads value from value-string"

  (from-value-string (parse/parse-string "(+ 1 1)"))
  => '(+ 1 1))

^{:refer std.block.value/list-value :added "3.0"}
(fact "returns the value of an :list block"

  (list-value (parse/parse-string "(+ 1 1)"))
  => '(+ 1 1))

^{:refer std.block.value/map-value :added "3.0"}
(fact "returns the value of an :map block"

  (map-value (parse/parse-string "{1 2 3 4}"))
  => {1 2, 3 4}

  (map-value (parse/parse-string "{1 2 3}"))
  => (throws))

^{:refer std.block.value/set-value :added "3.0"}
(fact "returns the value of an :set block"

  (set-value (parse/parse-string "#{1 2 3 4}"))
  => #{1 4 3 2})

^{:refer std.block.value/vector-value :added "3.0"}
(fact "returns the value of an :vector block"

  (vector-value (parse/parse-string "[1 2 3 4]"))
  => [1 2 3 4])

^{:refer std.block.value/deref-value :added "3.0"}
(fact "returns the value of a :deref block"

  (deref-value (parse/parse-string "@hello"))
  => '(deref hello))

^{:refer std.block.value/meta-value :added "3.0"}
(fact "returns the value of a :meta block"

  ((juxt meta identity)
   (meta-value (parse/parse-string "^:dynamic {:a 1}")))
  => [{:dynamic true} {:a 1}]

  ((juxt meta identity)
   (meta-value (parse/parse-string "^String {:a 1}")))
  => [{:tag 'String} {:a 1}])

^{:refer std.block.value/quote-value :added "3.0"}
(fact "returns the value of a :quote block"

  (quote-value (parse/parse-string "'hello"))
  => '(quote hello))

^{:refer std.block.value/var-value :added "3.0"}
(fact "returns the value of a :var block"

  (var-value (parse/parse-string "#'hello"))
  => '(var hello))

^{:refer std.block.value/hash-keyword-value :added "3.0"}
(fact "returns the value of a :hash-keyword block"

  (hash-keyword-value (parse/parse-string "#:hello{:a 1 :b 2}"))
  => #:hello{:b 2, :a 1})

^{:refer std.block.value/select-value :added "3.0"}
(fact "returns the value of a :select block"

  (select-value (parse/parse-string "#?(:clj hello)"))
  => '(? {:clj hello}))

^{:refer std.block.value/select-splice-value :added "3.0"}
(fact "returns the value of a :select-splice block"

  (select-splice-value (parse/parse-string "#?@(:clj hello)"))
  => '(?-splicing {:clj hello}))

^{:refer std.block.value/unquote-value :added "3.0"}
(fact "returns the value of a :unquote block"

  (unquote-value (parse/parse-string "~hello"))
  => '(unquote hello))

^{:refer std.block.value/unquote-splice-value :added "3.0"}
(fact "returns the value of a :unquote-splice block"

  (unquote-splice-value (parse/parse-string "~@hello"))
  => '(unquote-splicing hello))

(comment
  (base/block-value (parse/parse-string "#= #= #= #=(list list list + 1 2))")))
