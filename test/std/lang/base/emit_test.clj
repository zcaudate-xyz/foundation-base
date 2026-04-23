(ns std.lang.base.emit-test
  (:require [std.lang.base.book-entry :as entry]
             [std.lang.base.emit :as emit :refer :all]
             [std.lang.base.emit-common :as common]
             [std.lang.base.emit-helper :as helper]
             [std.lang.base.emit-prep-lua-test :as prep]
             [std.lang.base.grammar :as grammar]
             [std.lib.walk :as walk])
  (:use code.test))

(def +reserved+
  (-> (grammar/build)
      (grammar/to-reserved)))

(def +grammar+
  (grammar/grammar :test +reserved+ helper/+default+))

(defn emit-rewrite-probe
  [form _ _]
  (walk/postwalk (fn [x]
                   (if (= x '(+ 1 2 3))
                     '(+ 1 2 3 4)
                     x))
                 form))

(def +rewrite-grammar+
  (grammar/grammar :rewrite +reserved+ (assoc helper/+default+
                                              :rewrite [#'emit-rewrite-probe])))

^{:refer std.lang.base.emit/default-grammar :added "4.0"}
(fact "returns the default grammar"

  (emit/default-grammar)
  => map?)

^{:refer std.lang.base.emit/emit-main-loop :added "4.0"}
(fact "creates the raw emit"

  (emit/emit-main-loop '(not (+ 1 2 3))
                      +grammar+
                      {})
  => "!((+ 1 2 3))")

^{:refer std.lang.base.emit/emit-main :added "4.0"}
(fact "creates the raw emit with loop"

  (emit/emit-main '(not (+ 1 2 3))
                  +grammar+
                  {})
  => "!(1 + 2 + 3)")

^{:refer std.lang.base.emit/emit :added "4.0"}
(fact "emits form to output string"

  (emit/emit '(+ 1 2 3)
             @+test-grammar+
             nil
             {})
  => "1 + 2 + 3")

^{:refer std.lang.base.emit/with:emit :added "4.0"}
(fact "binds the top-level emit function to common/*emit-fn*"

  (emit/with:emit
   (common/*emit-fn* '(not (+ 1 2 3))
                     +grammar+
                     {}))
  => "!(1 + 2 + 3)")

^{:refer std.lang.base.emit/prep-options :added "4.0"}
(fact "prepares the options for processing"

  (prep-options {})
  => vector?

  (first (prep-options {:lang :lua
                        :book prep/+book-min+}))
  => :code)

^{:refer std.lang.base.emit/prep-form :added "4.0"}
(fact "prepares the form"

  (prep-form :raw '(+ 1 2 3) nil nil {})
  => '[(+ 1 2 3)]

  (prep-form :input '(+ @1 2 3) nil nil {})
  => '[(+ (!:eval 1) 2 3)]

  (prep-form :staging '(+ @1 2 3) nil nil {})
  => '[(+ (!:eval 1) 2 3) #{} #{} {}])

(fact "code preparation applies the rewrite phase"
  (prep-form :code '(do (+ 1 2 3))
             +rewrite-grammar+
             nil
             '{:lang :lua
               :module {:id L.core
                        :link {}}})
  => '[(do (+ 1 2 3 4)) #{} #{} {}])
